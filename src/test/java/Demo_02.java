import org.activiti.engine.*;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CRC
 * @date 2019-10-09
 * 角色分配
 */
public class Demo_02 {

    private final String key = "groupTaskDelegate";//key值
    private final int version = 1;//版本号

    //Service接口的父类，可以直接获取下边的Service
    private ProcessEngine processEngine;
    //Activiti的七大Service类
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private HistoryService historyService;
    private ManagementService managementService;
    private TaskService taskService;
    private IdentityService identityService;
    private FormService formService;

    @Before
    public void init() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-activiti.xml");
        processEngine = ProcessEngines.getDefaultProcessEngine();
        repositoryService = (RepositoryService) context.getBean("repositoryService");//获取bean中的对象
        runtimeService = (RuntimeService) context.getBean("runtimeService");//获取bean中的对象
        historyService = (HistoryService) context.getBean("historyService");//获取bean中的对象
        managementService = (ManagementService) context.getBean("managementService");//获取bean中的对象
        identityService = (IdentityService) context.getBean("identityService");//获取bean中的对象
        formService = (FormService) context.getBean("formService");//获取bean中的对象
        taskService = processEngine.getTaskService();
        System.out.println("========== 初始化完成 ==========");
    }

    /**
     * 流程定义(流程图的设计)好以后,需要部署
     * 流程部署涉及到的几个动作和表
     * 流程部署表 act_re_deployment
     * 流程定义表 act_re_procdef
     * 流程资源表 act_ge_bytearray
     */
    @Test
    public void complateTask() {
        Deployment deploy = processEngine.getRepositoryService()
                .createDeployment()
                .name("Activiti角色配置流程")//部署上去名字
                .addClasspathResource("leave.bpmn")//bpmn文件路径
                .addClasspathResource("leave.png")//流程图
                .deploy();//部署
        System.out.println("流程实例Id--->" + deploy.getId());
        System.out.println("流程实例Name--->" + deploy.getName());
    }

    /**
     * 创建 角色/分组
     */
    @Test
    public void testProcessDeploy() {
        IdentityService identityService = processEngine.getIdentityService();

        //创建用户
        identityService.saveUser(new UserEntity("张三"));
        identityService.saveUser(new UserEntity("李四"));
        identityService.saveUser(new UserEntity("王五"));
        identityService.saveUser(new UserEntity("赵六"));
        identityService.saveUser(new UserEntity("田七"));
        identityService.saveUser(new UserEntity("胡八"));
        //创建角色
        identityService.saveGroup(new GroupEntity("部门主管"));
        identityService.saveGroup(new GroupEntity("部门经理"));
        identityService.saveGroup(new GroupEntity("CTO"));
        System.out.println("角色创建完成");

        //创建角色和用户的对应关系
        identityService.createMembership("张三", "部门主管");
        identityService.createMembership("李四", "部门主管");
        identityService.createMembership("王五", "部门经理");
        identityService.createMembership("赵六", "部门经理");
        identityService.createMembership("田七", "CTO");
        identityService.createMembership("胡八", "CTO");
        System.out.println("角色关系分配完成");
    }


    /**
     * 流程的启动(流程实例的产生) new Class->Object
     * 涉及到的表 :
     * act_ru_execution  流程实例表
     * act_ru_task 会产生一条待执行的任务记录
     * act_hi_taskinst 也会产生一条历史任务记录(注意:endtime is null)
     * 注意: 以流程定义的key启动的话,默认会进入版本最新的流程
     */
    @Test
    public void testStartProcess() {
        //根据key和version查出对应版本的实例Id
        String InstanceId = findProcessDefinition(key, version);
        //流程定义的key启动的话,默认会进入版本最新的流程
        for (int i = 0; i < 5; i++) {
            System.out.println("启动第--> " + i + " 个流程任务");
            ProcessInstance processInstance = processEngine.getRuntimeService() //与流程实例运行相关的服务类
                    .startProcessInstanceById(InstanceId,key);//启动流程 ,生成一个流程实例
            System.out.println("流程部署的ID:" + processInstance.getDeploymentId());
            System.out.println("流程定义的ID:" + processInstance.getProcessDefinitionId());
            System.out.println("流程实例的ID:" + processInstance.getProcessInstanceId());
        }
        System.out.println("流程启动完毕");
    }


    /**
     * 查询某组的任务
     */
    @Test
    public void testQueryMyGroupTask() {
        String assignee = "李四";
        String processinstanceId = "30021";
        List<Task> taskList = processEngine.getTaskService()  //跟任务处理相关的服务类
                .createTaskQuery() //创建一个任务查询
                .taskCandidateUser(assignee) //加入查询条件
//                .processInstanceId(processinstanceId)//查询某个任务
                .list(); //返回形式

        if (taskList != null && taskList.size() > 0) {
            for (Task task : taskList) {
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("流程实例ID:" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("任务ID:" + task.getId());//任务ID:130004
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());

            }
        }
    }

    /**
     * 流程处理过程：查询个人任务
     * 处理流程的步骤:查询个人任务 完成个人任务
     * 涉及到的表：act_ru_task
     */
    @Test
    public void testQueryMyTask() {
        String assignee = "张三";
//        String processinstanceId = "5014";
        List<Task> taskList = processEngine.getTaskService()  //跟任务处理相关的服务类
                .createTaskQuery() //创建一个任务查询
                .taskAssignee(assignee) //加入查询条件
//                .processInstanceId(processinstanceId)
                .list(); //返回形式

        if (taskList != null && taskList.size() > 0) {
            for (Task task : taskList) {
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("流程实例ID:" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("任务ID:" + task.getId());//任务ID:10004
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("==================");
            }
        }
    }




    /**
     * 因为一组人都具有任务权限 ,我们得到任务ID之后,可以根据任务ID来查询哪些人具有此次任务的处理权限
     * 异常  暂不使用
     */
//    @Test
//    public void testQueryGroupUserByTaskId() {
//        String taskId = "5004";
//        List<IdentityLink> identityLinkList = taskService.getIdentityLinksForTask(taskId);
//        if (identityLinkList != null && identityLinkList.size() > 0) {
//            for (IdentityLink identityLink : identityLinkList) {
//                System.out.println(identityLink.getGroupId());
//                System.out.println(identityLink.getUserId());
//                System.out.println(identityLink.getTaskId());
//            }
//        }
//
//    }




    /**
     * 人工节点通过方法
     * 流程处理过程：完成个人任务
     * 处理流程的步骤:查询个人任务 完成个人任务
     * 涉及到的表：act_ru_task
     */
    @Test
    public void testCompleteMyTask() {
        String taskId = "22504";
        Map<String, Object> mapVariables = new HashMap<String, Object>();
//        mapVariables.put("manageId01", "部门主管");
//        mapVariables.put("mes01", "同意，好好干，年底发红包");

        mapVariables.put("manageId02", "部门经理");
        mapVariables.put("mes02", "同意，好好干，年底发奖金");

//        mapVariables.put("manageId03", "部门CTO");
//        mapVariables.put("mes03", "同意，好好干，年底发分红");
        //带审核意见
        processEngine.getTaskService().complete(taskId, mapVariables);
        System.out.println("审批完成");

        //不带审核意见
//	   String taskId = "25002";
//	   processEngine.getTaskService().complete(taskId);
//	   System.out.println("审批任务完成");
    }

    /**
     * 自动审核通过节点
     */
    @Test
    public void testCompleteMyTaskSelf(){
        String processDefinId = findProcessDefinition(key, version);
        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinId,key);//启动流程 ,生成一个流程实例
        System.out.println("流程定义的ID:" + instance.getProcessDefinitionId());
        System.out.println("流程实例的ID:" + instance.getProcessInstanceId());

        Task task = taskService.createTaskQuery().processInstanceId(instance.getProcessInstanceId()).active().singleResult();
        while (task!=null){
            System.out.println("============"+task.getName()+" 流程开始 ============");
            Map<String, Object> mapVariables = new HashMap<String, Object>();
            if ("usertask1".equals(task.getTaskDefinitionKey())){
                mapVariables.put("manageId01", "部门主管");
                mapVariables.put("mes01", "同意，好好干，年底发红包");
            }
            if ("usertask2".equals(task.getTaskDefinitionKey())){
                mapVariables.put("manageId02", "部门经理");
                mapVariables.put("mes02", "同意，好好干，年底发奖金");
            }
            if ("usertask3".equals(task.getTaskDefinitionKey())){
                mapVariables.put("manageId03", "部门CTO");
                mapVariables.put("mes03", "同意，好好干，年底发分红");
            }
            System.out.println("============"+task.getName()+" 流程结束 ============");
            //带审核意见
            processEngine.getTaskService().complete(task.getId(), mapVariables);
            //重新获取task
            task = taskService.createTaskQuery().processInstanceId(instance.getProcessInstanceId()).active().singleResult();
        }

        System.out.println("审批完成");
    }



    /**
     * 在流程的执行过程中,我们需要查询流程执行到了那一个状态
     */
    @Test
    public void testQueryProinstanceState() {
        String processInstanceId = "30001";
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance != null) {
            System.out.println("当前流程执行到:" + processInstance.getActivityId());
        } else {
            System.out.println("当前流程已结束");

        }
    }


    /**
     * 任务拾取
     * 我们设置的任务处理人员暂时都只是候选人 ,并不是实际处理人,必须经过任务拾取的过程来确定谁来处理任务 (task assignee)
     * 任务拾取的过程,就是给执行任务表指定assginee字段值的过程
     */
    @Test
    public void claimTask() {
        String taskId = "2504";
        String userId = "张三";
        taskService.claim(taskId, userId);

        //任务拾取以后, 可以回退给组
        //processEngine.getTaskService().setAssignee(taskId, null);
        //任务拾取以后,可以转给别人去处理(别人可以是组成员也可以不是)
        //processEngine.getTaskService().claim(taskId, "xiaoxi");
    }



    /**
     * 根据传入流程的key和版本号查询流程的ID
     */
    public String findProcessDefinition(String processDefinitionKey, int processVersion) {
        String processDefinitionId = "";
        System.out.println("===1.processEngine==" + processEngine);
        System.out.println("===2.repositoryService==" + repositoryService);
        List<ProcessDefinition> list = repositoryService// 与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()// 创建一个流程定义查询
                .processDefinitionKey(processDefinitionKey)// 使用流程定义的KEY查询
                .orderByProcessDefinitionVersion().asc()// 按照版本的升序排列
                .list();// 返回一个集合列表，封装流程定义
        if (list != null && list.size() > 0) {
            for (ProcessDefinition processDefinition : list) {
                if (processVersion == processDefinition.getVersion()) {
                    processDefinitionId = processDefinition.getId();
                }
            }
        }
        return processDefinitionId;
    }


}
