package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.cmd.BackAndJumpCmd;
import com.example.demo.cmd.CountersignAddcmd;
import com.example.demo.cmd.SequentialAndParallelCountersignAddcmd;
import com.example.demo.cmd.SerialCountersignAddcmd;
import com.example.demo.entity.MyJsonDateObject;
import com.example.demo.entity.MyProcessDefinition;
import com.example.demo.entity.MyProcessInstance;
import com.example.demo.entity.Mycustom;
import com.example.demo.entity.Mytask;
import com.example.demo.entity.UserTaskActivity;
import com.example.demo.entity.UserTaskProDef;
import com.example.demo.mycustom.MycustomService;
import com.example.demo.utils.JsonUtil;
import com.example.demo.utils.ProcessDefinitionTOmyProcessDefinition;
import com.example.demo.utils.ProcessInstanceTOmyProcessInstance;
import com.example.demo.utils.Rollback;
import com.example.demo.utils.TaskInfoUtils;
import com.example.demo.utils.TasktoMyTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/activiti/test1")
public class Test1Controller {
	RuntimeServiceImpl a;
	TaskServiceImpl t;
	//ProcessEngineConfigurationImpl ProcessEngineConfigurationImpl;
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    private ManagementService managementService;
    /**
     * 查询已经部署的流程定义（获取key）
     * （流程定义表act_re_procdef）
     * @return	List<MyProcessDefinition>
     */
    @ApiOperation(value = "ProcessDefinitionQueryKEY", notes = "流程定义查询key")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/ProcessDefinitionQuery")
    public List<MyProcessDefinition> ProcessDefinitionQuery() {
    	List<ProcessDefinition> list =repositoryService.createProcessDefinitionQuery()
										.orderByProcessDefinitionVersion().desc()
										.list();
        return ProcessDefinitionTOmyProcessDefinition.convertMyProcessDefinition(list);
    }
    /**
     * 获取流程定义信息中用户任务走向
     * @param processDefId 流程定义id&key
     * @return List
     */
    @ApiOperation(value = "userTasks", notes = "获取流程定义信息中用户任务走向", position = 1100)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/userTasks/{processDefIdkey}")
    public List<UserTaskActivity> userTasks(@PathVariable(value = "processDefIdkey") String processDefIdkey) {
        List<SequenceFlow> sequenceFlowList = new ArrayList<>();
        List<UserTask> userTaskList = new ArrayList<>();
        List<Gateway> gatewayList = new ArrayList<>();
        StartEvent startEvent=new StartEvent();
        List<ProcessDefinition> processDefList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefIdkey).active().latestVersion().list();

        if (processDefList.size() > 0) {
            BpmnModel model = repositoryService.getBpmnModel(processDefList.get(0).getId());
            if (model != null) {
                Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                	if(e instanceof StartEvent) {
                		startEvent=(StartEvent)e;
                	}
                	//如果是线
                	if (e instanceof SequenceFlow) {
                		SequenceFlow sequenceFlow=(SequenceFlow) e;
                		sequenceFlowList.add(sequenceFlow);
                    }
                	//如果是任务节点
                	if (e instanceof UserTask) {
                		UserTask userTask=(UserTask) e;
                		userTaskList.add(userTask);
                    }
                	//如果是网关
                	if(e instanceof Gateway) {
                		gatewayList.add((Gateway)e);
                	}
                }
            }
        }
//        return result;
        return TaskInfoUtils.userTaskGetPosition(startEvent, sequenceFlowList, userTaskList, gatewayList);
    }
    /**
     * 获取流程定义信息中用户任务走向
     * @param processDefId 流程定义id&key
     * @return List
     */
    @ApiOperation(value = "userTasks", notes = "获取流程定义信息中用户任务走向", position = 1100)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/userTasks1/{processDefIdkey}")
    public List<UserTaskProDef> userTasks1(@PathVariable(value = "processDefIdkey") String processDefIdkey) {
        List<SequenceFlow> sequenceFlowList = new ArrayList<>();
        List<UserTask> userTaskList = new ArrayList<>();
        List<Gateway> gatewayList = new ArrayList<>();
        StartEvent startEvent=new StartEvent();
        List<ProcessDefinition> processDefList = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefIdkey).active().latestVersion().list();

        if (processDefList.size() > 0) {
            BpmnModel model = repositoryService.getBpmnModel(processDefList.get(0).getId());
            if (model != null) {
                Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
                for (FlowElement e : flowElements) {
                	if(e instanceof StartEvent) {
                		startEvent=(StartEvent)e;
                	}
                	//如果是线
                	if (e instanceof SequenceFlow) {
                		SequenceFlow sequenceFlow=(SequenceFlow) e;
                		sequenceFlowList.add(sequenceFlow);
                    }
                	//如果是任务节点
                	if (e instanceof UserTask) {
                		UserTask userTask=(UserTask) e;
                		userTaskList.add(userTask);
                    }
                	//如果是网关
                	if(e instanceof Gateway) {
                		gatewayList.add((Gateway)e);
                	}
                }
            }
        }
//        return result;
        return TaskInfoUtils.userTaskGetPosition1(startEvent, sequenceFlowList, userTaskList, gatewayList);
    }
    
	/**
     * 流程定义Key启动并判断是否完成第一个任务
     * @param processDefIdkey （流程定义Key）
     * @param startUser （启动人）
     * @param startAndCompleteFirst	（是否完成第一个任务）
     * @param data	（入参Requestbody传入）
     * @return Mytask（返回第一个任务）
     */
    @ApiOperation(value = "startProcess", notes = "启动流程")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value="/startProcess/{processDefIdkey}",produces=MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Mytask start(@PathVariable(value = "processDefIdkey") String processDefIdkey,
                            @RequestParam(value = "startUser") String startUser, 
                            @RequestParam(value = "startAndCompleteFirst") boolean startAndCompleteFirst,
                            @RequestBody Map<String, Object> data) {
        return internalStartProcess(processDefIdkey, startUser, data, startAndCompleteFirst);
    }
	private Mytask internalStartProcess(String processDefIdkey, String startUser, Map<String, Object> data,
			boolean startAndCompleteFirst) {
		//1，将入参放入map集合中
		Map<String, Object> vars = new HashMap<>();
        vars.putAll(data);
        vars.put("startUser", startUser);
        //2，设置开启人
		identityService.setAuthenticatedUserId(startUser);
		//3，启动流程（返回流程实例对象：对应act_ru_execution表记录对象）
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processDefIdkey, vars);
        //4，根据当前流程实例ID获取当前活动的任务
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).active().singleResult();
        //5，是否完成该任务
        if (startAndCompleteFirst) {
        	//6，查看办理人是否为空，将启动人设为办理人
            if (task.getAssignee() == null) {
                taskService.setAssignee(task.getId(), startUser);
            }
            taskService.complete(task.getId());
        }
        //不管完成与否，都会返回第一个任务
		return TasktoMyTask.build(task);
	}
	/**
     * 查询开启的流程实例（获取流程实例ID）
     * （正在执行的流程表act_ru_execution）
     * @return	List<MyProcessInstance>
     */
    @ApiOperation(value = "ProcessInstanceQueryID", notes = "流程定义查询key")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/ProcessInstanceQuery")
    public List<MyProcessInstance> ProcessInstanceQuery() {
    	List<ProcessInstance> list =runtimeService.createProcessInstanceQuery()
    									.orderByProcessDefinitionKey().desc().list();
        return ProcessInstanceTOmyProcessInstance.convertMyProcessInstance(list);
    }
	/**
     * 流程实例Id查活动任务
     * @param processInstanceId
     * @return	List<Mytask>
     */
    @ApiOperation(value = "activeTasks", notes = "活动任务查询")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/activeTasks/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Mytask> activeTasks(@PathVariable String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        return TasktoMyTask.convertMytask(tasks);
    }
    /**
     * 完成任务
     * @param taskId （任务ID）
     * @param data	（入参Requestbody传入）
     * @return	String
     */
    @ApiOperation(value = "completeTask", notes = "完成/提交任务")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/completeTask/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String completeTask(@PathVariable String taskId, 
    						 	@RequestBody Map<String, Object> data) {
        Map<String, Object> vars = new HashMap<>();
        vars.putAll(data);
        //（false时）将vars存为整个流程实例的流程变量
        taskService.complete(taskId, vars, false);
        return "成功完成任务";
    }
	/**
     * 流程实例Id查历史任务（已完成和待完成的一个或者是全部完成的任务）
     * @param processInstanceId
     * @return	List<Mytask>
     */
    @ApiOperation(value = "historyTasksQuery", notes = "历史任务查询")
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/historyTasksQuery/{processInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Mytask> historyTasksQuery(@PathVariable String processInstanceId) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        					.processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();//这里按照创建时间排序
        return TasktoMyTask.convertMytask2(tasks);
    }
    /**
     * 任务指定办理人
     * @param taskId   任务id
     * @param assignee 用户Id
     */
    @ApiOperation(value = "taskClaim", notes = "任务指定用户", position = 1200)
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/taskClaim/{taskId}/{assignee}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void taskClaim(@PathVariable(value = "taskId") String taskId,
                          @PathVariable(value = "assignee") String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            taskService.setAssignee(taskId, assignee);//将原有的办理人覆盖
        } else {
            throw new IllegalArgumentException(taskId + "任务不存在");
        }
    }

    /**
     * 任务转办
     * @param taskId   任务id
     * @param assignee 用户Id
     */
    @ApiOperation(value = "taskTransfer", notes = "任务转办", position = 1200)
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/taskTransfer/{taskId}/{assignee}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void taskTransfer(@PathVariable(value = "taskId") String taskId,
                             @PathVariable(value = "assignee") String assignee,
                             @RequestBody Map<String, Object> data) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
        	taskService.setOwner(taskId, task.getAssignee());//设置任务之前拥有者
            if (data != null) {
                taskService.setVariables(taskId, data);
                taskService.setVariablesLocal(taskId, data);
            }
            taskService.setAssignee(taskId, assignee);//设置任务办理人
        }else {
            throw new IllegalArgumentException(taskId + "任务不存在");
        }
    }
    /**
     * 任务声明办理人（之前没有办理人）
     * @param taskId   任务id
     * @param assignee 用户Id
     */
    @ApiOperation(value = "taskClaim2", notes = "任务指定用户", position = 1200)
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/taskClaim2/{taskId}/{assignee}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void taskClaim(@PathVariable(value = "taskId") String taskId,
                          @PathVariable(value = "assignee") String assignee,
                          @RequestBody Map<String, Object> data) {
        this.taskClaim(taskId, assignee);//之前没有办理人，声明办理人
        if (data != null) {
            taskService.setVariables(taskId, data);
            taskService.setVariablesLocal(taskId, data);
        }
    }
    /**
     * 转化流程定义为model可用的xml文件(bpmn文件)
     * @param processDefId
     * @return
     */
    @ApiOperation(value = "getBpmnxml", notes = "转化流程定义为editor可用的xml文件", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/bpmntoxml/{processDefId}", produces = MediaType.APPLICATION_XML_VALUE)
    public String getBpmnxml(@PathVariable(value="processDefId") String processDefIdkey) {
        BpmnModel model = repositoryService.getBpmnModel(processDefIdkey);
        return new String(new BpmnXMLConverter().convertToXML(model));
    }
    

    /**
     * 任务跳转回退（方式一）
     * 不删除任何历史记录（可以在历史表中标明任务跳转节点）
     * 改变流程定义的任务出线，完成任务后自动回退到之前的节点
     */
    @Autowired
    Rollback rollback;
    @PostMapping(value = "/taskJump/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mytask taskJump1(@PathVariable(value = "taskId") String taskId,
    						@RequestParam(value = "jumpTotaskdefkey") String jumpTotaskdefkey) {
    	rollback.rollBackToAssignWorkFlow(taskId, jumpTotaskdefkey);
        Task task=taskService.createTaskQuery().taskDefinitionKey(jumpTotaskdefkey).active().singleResult();
        return TasktoMyTask.build(task);
    }
    /**
     * 任务跳转回退（方式二）
     * 编写命令：将当前任务完成或者删除，针对跳转活动节点新建任务
     */
    @PostMapping(value = "/taskJump2/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String taskJump2(@PathVariable(value = "taskId") String taskId,
    						@RequestParam(value = "jumpToActivityId") String jumpToActivityId,
    						@RequestParam(value = "operate") String operate,
    						@RequestBody Map<String, Object> data) {
        return managementService.executeCommand(new BackAndJumpCmd(taskId,jumpToActivityId,operate,data));
    }

    @Autowired
	MycustomService mycustomService;
    @ApiOperation(value = "customMappers", notes = "自定义操作数据库", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public String customMappers() {
    	Mycustom mycustom = new Mycustom();
		mycustom.setId("1211");
		mycustom.setDis("sas222");
		mycustom.setName("alksdm222");
		mycustomService.insert0(mycustom);
		return "66666666666";
    }
    /**
     * model是前端编辑页面显示的界面流程图
     * 导入bpmn文件到model中
     * 导入json格式的流程定义文件
     * 导出json格式的流程定义文件
     * 创建新模型
     * 删除model，如果model已经发布（deploy）则不能删除（发布的意思是部署了的流程定义）
     * 获取model的分组
     * 获取模板id对应的流程
     * 获取流程实例
     * 获取流程节点信息
     */
    @ApiOperation(value = "selectVariable", notes = "查流程变量", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/selectVariable/{taskID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> selectVariable(@PathVariable(value = "taskID") String taskID) {
    	Task task=taskService.createTaskQuery().taskId(taskID).active().singleResult();
    	System.out.println(task.getExecutionId());
    	return runtimeService.getVariables(task.getExecutionId());
    	//return runtimeService.getVariablesLocal(task.getExecutionId());
    }
    @ApiOperation(value = "selectExecution", notes = "查执行实例", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/selectExecution/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String selectExecution(@PathVariable(value = "taskId") String taskId) {
//    	Task task=taskService.createTaskQuery().taskId(taskId).active().singleResult();
    	Execution excution= runtimeService.createExecutionQuery().executionId("120010").singleResult();
    	Execution parent= runtimeService.createExecutionQuery().executionId("120001").singleResult();;
    	return excution.toString()+parent.toString();
    }
    @ApiOperation(value = "addAssignee", notes = "并行会签加签", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/addAssignee/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addAssignee(@PathVariable(value = "taskId") String taskId,
    							@RequestParam(value = "assignee") String assignee) {
    	managementService.executeCommand(new CountersignAddcmd(taskId,assignee,runtimeService,taskService));
    	return "并行加签成功";
    }
    @ApiOperation(value = "addAssignee2", notes = "串行会签加签", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/addAssignee2/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addAssignee2(@PathVariable(value = "taskId") String taskId,
    							@RequestParam(value = "assignee") String assignee,
    							@RequestParam(value = "isBefore") Boolean isBefore) {
    	managementService.executeCommand(new SerialCountersignAddcmd(taskId,assignee,runtimeService,taskService,isBefore));
    	return "串行前或后加签成功";
    }
    /**
     * 串并行会签加签
     * @param taskId
     * @param assignee   只支持单人
     * @param isBefore
     * @return
     */
    @ApiOperation(value = "addAssignee3", notes = "串并行会签加签", position = 2000)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/addAssignee3/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addAssignee3(@PathVariable(value = "taskId") String taskId,
                               @RequestParam(value = "assignee") String assignee,
                               @RequestParam(value = "isBefore",defaultValue = "0",required=false) Boolean isBefore) {
        return managementService.executeCommand(new SequentialAndParallelCountersignAddcmd(taskId, assignee, runtimeService, taskService, isBefore));
    }
    /**
     * 解析resource下的.json文件中的对象或数组对象
     * 使用的是net.sf.json-lib提供的方法
     * 引入依赖<dependency>
     *     		<groupId>net.sf.json-lib</groupId>
     *         	<artifactId>json-lib</artifactId>
     *          <version>2.4</version>
     *          <classifier>jdk15</classifier>
     *        </dependency>
     * @return List<MyJsonDateObject>
     */
    @ApiOperation(value = "getJsonResource", notes = "解析resource下的.json文件", position = 1300)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/getJsonResource", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MyJsonDateObject> getJsonResource(){
        List<MyJsonDateObject> list = new ArrayList<>();
        String path = getClass().getClassLoader().getResource("i18n-set.json").toString();
        path = path.replace("\"", "/");
        if(path.contains(":")){
            path = path.replace("file:/","");
        }
        try {
			String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
			
			String jsonStr2=JSONObject.fromObject(jsonStr).getString("lang");
			list=JsonUtil.jsonStrToBeanList(jsonStr2,MyJsonDateObject.class);       
		} catch (IOException e) {
			e.printStackTrace();
		}
        return list;
    }
    /**
     * 解析resource下的.json文件
     * 使用com.fasterxml.jackson.databind提供的方法
     * 根据节点树解析json文件
     * 
     * @return
     */
    @ApiOperation(value = "getJsonResource2", notes = "解析resource下的.json文件", position = 1300)
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/getJsonResource2", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MyJsonDateObject> getJsonResource2(){
    	 List<MyJsonDateObject> list = new ArrayList<>();
         String path = getClass().getClassLoader().getResource("i18n-set.json").toString();
         path = path.replace("\"", "/");//去掉双引号
         if(path.contains(":")){
             path = path.replace("file:/","");
         }
      try{
      String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
      ObjectMapper objectMapper=new ObjectMapper();
      JsonNode objectNode = objectMapper.readTree(jsonStr);
      JsonNode langNode = objectMapper.readTree(objectNode.get("lang").toString());
      for (int i = 0; i < langNode.size(); i++) {
      	MyJsonDateObject langResource = new MyJsonDateObject();
          langResource.setTitle(String.valueOf(langNode.get(i).get("title")).replace("\"",""));//.replace("\"","")
          langResource.setValue(String.valueOf(langNode.get(i).get("value")).replace("\"",""));
          list.add(langResource);
      }
      }catch (Exception e){
    	  e.printStackTrace();
      }
    	return list;
    }
    /**
     * 单个文件上传
     * 保存在指定文件夹
     * 
     * @param file
     */
    @ApiOperation(value = "uploadFile", notes = "文件上传，保存在f盘下")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/uploadFile", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadFile(@RequestParam(value = "file", required = false) MultipartFile file){
    	if(!file.isEmpty()) {
			String fileName = new Date().getTime()+file.getOriginalFilename();	
			//放在f盘下的aaa目录下
			String path = "F:\\aaa\\";
			File dest = new File(path + fileName);
			try {
				file.transferTo(dest);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}	
    }
    /**
     * 多个文件上传
     * 保存在指定文件夹
     * SpringBoot有大小限制单个文件不超过1M，多个文件不超过10M
     * 修改：application.properties中加配置spring.servlet.multipart.max-file-size=10MB
     * 			限制单个文件和总大小		  spring.servlet.multipart.max-request-size=100MB
     * 
     * @param request
     * @return
     */
    @ApiOperation(value = "uploadMultiFile", notes = "多文件上传，保存在f盘下")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/uploadMultiFile", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadMultiFile(HttpServletRequest request){
    	MultipartHttpServletRequest newrequest=(MultipartHttpServletRequest)request;
    	List<MultipartFile> files=newrequest.getFiles("file");
    	//放在f盘下的aaa目录下
		String path = "F:\\aaa\\";
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                return "上传第" + (i++) + "个文件失败";
            }
            String fileName = new Date().getTime()+file.getOriginalFilename();
            File dest = new File(path + fileName);
            try {
				file.transferTo(dest);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return "success";
    }
    /**
     * 文件下载
     * 根据文件名下载类路径resources下的文件
     * @param response
     * @param fileName
     * @throws IOException 
     */
	@ApiOperation(value = "downloadFile", notes = "文件下载")
	@ResponseStatus(value = HttpStatus.OK)
	@GetMapping(value = "/downloadFile", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> downloadFile(@RequestParam(value = "fileName", required = false) String fileName) throws IOException {
		Resource resource = new ClassPathResource(fileName);
		File file = null;
		if(resource.exists()) {
	        file = resource.getFile();
		}else {
			return null;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("utf-8"), "iso-8859-1"));//文件名
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);// 文件内容是字节流

		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
	}
}
