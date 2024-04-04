# FlexiTeamsWorkflows

# 1. Simulation
- the simulation uses a **TimeManager** which starts after **runSimulation Method** in ***Simulation Manager*** has been called  
- this Time Manager is used to determine at what second events (property: starttime) occur, and when tasks are finished with execution  
- the **Simulation Manager** schedules Events to the ***currently single supported Workflow***
- from here most logic is implemented in the **Workflow class**  
- within this class there is the **main method** ***run Workflow*** which runs ***without determination*** currently  
- it calls the methods **planExecution** and **startExecution**
- there is a helper class, the **TaskRunConcept(TRC)** which combines an event, a task, and a Thread,
this is the core for **planning** and **executing** Tasks with events, and is used in the **waitingTasks** and **runningTasks** List of Workflows
- a **TRC** is created whenever a event is ready to be planned with a task, linking the task, the event, and a thread that **will** execute the task, for more information about TRC check Java Class
### 1.1 planExecution
the planning phase consist of **four parts** which should be run **in this exact order**
1. check running Task Map for TRC's that are finished, collect task names
2. run through all collected taskNames
   1. take TRC out of running tasks, save event to var
   2. add task name to free tasks 
   3. let ResourceManager free the resource used by this task 
   4. if no follower Tasks -> check if every predecessor has processed the event if yes -> events has reached end else -> nothing, next time
   5. if follower Tasks -> instantiate TRC for every follower task, whose predeccesors all have processed the event else -> some info printing is done
   6. add TRC to **waitingTasks**
3. for every event that was scheduled by the simulation manager plan its first execution by instantiateing a TRC with the event, the start task of the workflow and put it in waiting Tasks
4. let Resource manager reevaluate the waitingForResources List, as Reosurces may have been freed in step 1

### 1.2 startExecution
the startExecution method currently loops through all keys of waitingTasksMap which holds every taskName as key and a List of waiting TRC for every taskName  
there are unnecessary iterations as there is not for every task a trc waiting, think of the start of the simulation where there is only a TRC waiting for the first task  
so with another list containing only taskNames where there is actually some TRC waiting these unnecessary iterations could be jumped
Anyway, with this implementation the first condition checks for
1. is this task free
2. is a TRC waiting to be executed on this task
3. this task is not on the waiting List for resources //have in mind we just evaluated this list in the previous method, if this task is still in the list, the necessary resources are not free   



if these conditions are met we ***peek*** the TRC contained in waiting tasks for this taskName to check the next conditions
1. we pass the TRC to the ResourceManagers method checkResourceAssertionPossible(trc, bind = true, taskName = null)
   1. if this runs true the ResourceManager did already bind the needed Resources to this task
      1. now we finally remove this trc from waitingTask by polling 
      2. remove the taskName from the freeTasks list 
      3. put this trc as value and the taskname as key in runningTasks 
      4. hit trc.start() to let the trc's thread execute the Task 
   2. if false, no resources were bound, we just put the taskName in the waitingForResources List
      1. the trc remains in the waitingTasks List and will only be considered next when the resources for this task are free next time




# 2. Importer
the importer imports every instance of the classes that are declared within the ontology e.g.  
  
- qualifications
- persons
- tasks
- resourceTypes
- events  

every instance of these classes that should be imported ***needs*** the according name property.  
without this property instances wont even be recognized by the importer. The importing of a instance may fail also if other needed 
properties are not used on the instance as they cant be used in the simulation either. 
This may be a reason for the importer to stop execution as missing Instances in the ontology may lead to faulty execution of
the simulation which nobody needs. Error prints are trying to give information on what caused the importer to stop execution

This README provides information about what properties are needed for classes and which are optional.  
***Always bear in mind that it can only be simulated what is modelled in the .owl***

## 3. Classes
Any of the classes mentioned below are part of the ontology, but for every one of them there is 
also a java class in this project, which is instantiated for any instance that is imported from the ontology
There are some restrictions
- only one workflow
- every instance needs all the properties mentioned at ***needed properties*** to be imported and not throw an error

### 3.1 Event
Any Workflow ahs events, events travel throug a workflow, like a patient in a
hospital,
a package in a distribution center etc.  
Events are not designed to be ***reoccuring***
#### Needed properties:

**name**(String): any event has a name, like an ID. It should be unique as it may work as an identifier.
A future version may give this ids automatically, but a value alongside the name property is required anyway
to be found by the interpreter  
**startTime**(int): the simTime where this event should occur  
**priority**(int): a priority for the event. if two events wait to be scheduled on a task, the event with the higher priority is chose,
if the same priority, lower starttime is chosen

### 3.2 ResourceType
A resource is something that is needed within a task to accomplish this task. These resources are modelled
to exclude persons with qualifications but everything else, mostly objects are meant by this
currently resources are needed to start a task but there is no logic for wear or any priorityizing. 
Resources may be changed within a task and should be used on the next task, this is also not modelled but all 
of this is absolutely achievable
#### Needed properties:

**name**(String): self declaring, should definitly be unique  
**unlimited/numberOfOccasions**(bool/int): two properties, one needs to be specified else fails, if unlimited = true and numberOfOccasions > 0 fails also  
- unlimited true means that whenever a task needs this resource one is created  
- numberOfOccasions > 0 lets the Importer know how many Resources of this Type are to be created

resources are treated as if they can only be at one task at a time, if there are only two resources 
but we are able to execute three tasks that all need one of this resource type then only two tasks can be startet

### 3.3 Qualification
this is meant to model skills of persons that are needed two execute a task a person may have multiple qualifications  
***this is currently not implemented for use and is a TODO***
#### Needed properties:

**name**(String): self declaring, should definitly be unique

### 3.4 Person
a person is meant to have one or many qualifications which qualificates a person to help executing a task
***this is currently not implemented for use and is a TODO***
#### Needed properties:

**name**(String): 
**qualification**(Instance): when modelling assign a qualification at hasQualification for any qualification this person should have 

### 3.5 Task
A task should occur only once in a workflow, if there is the same task twice in the workflow
specify it twice with different names f.e. task1, task2  
- currently no loops are supported  
- currently no conditional ways are supported
- a task can have multiple follower tasks, if it should not be end task
- a task can have multiple predecessor tasks, if it should not be a start task
- a task with no follower tasks is considered an end task
- a task with no predecessor task is considered an start task
- as no conditional ways are supported it is not possible for a task to act as end task or inner task on condition
- conditional ways is a TODO, would allow a task to be end task as well as inner task
#### Needed properties:

**name** (String): should be unique  
**priority** (int): this could be used to specify which task gets a resource/qualification if two tasks need the same limited resource/qualification at a time, currently this is not used  
**timeNeeded** (int): the sim time that a task needs to be finished
**qualificationsNeeded** (Instance): the qualifications a task needs to be started, currently not implemented, use taskNeedsQualification for every qualification that is needed here  
**resourcesNeeded** (Instance): the resource Types that are needed for the task to be started, implemented, use taskNeedsResource for every resourceType that is needed, if multiple Resources of that type are needed the ResourceType has to be linked via the property that certain amount  
**followingTasks** (Instance): all tasks that follow on this task, every single task is linked via taskHasFollower
**predecessorTasks** (Instance): all tasks that are predecessor of this task, every single task is linked via taskHasPredecessor

### 3.6 Workflow
A workflow contains the start task of a workflow, with the current implementation this can be only one
task, so no multiple entry points
- at the current implementation it is only taken care of one workflow, the ontology therefore should not contain more than one workflow
- 

#### Needed properties:

**name** (String): should be unique  


