import org.activiti.engine.*;
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
 * @date 2019-10-08
 * Activiti-Demo 测试类
 */
public class Demo_01 {

    private final String key = "demoTest";//key值
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
    public void init(){
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
     * 部署流程
     * 流程定义(流程图的设计)好以后,需要部署
     * 流程部署涉及到的几个动作和表
     * 流程部署表 act_re_deployment
     * 流程定义表 act_re_procdef
     * 流程资源表 act_ge_bytearray
     */
    @Test
    public void deploymentProcess(){
        Deployment deploy = processEngine.getRepositoryService()
                .createDeployment()
                .name("ActivitiDemo项目")//部署上去名字
                .addClasspathResource("ActTest.bpmn")//bpmn文件路径
                .deploy();//部署
        System.out.println("流程实例Id--->"+deploy.getId());
        System.out.println("流程实例Name--->"+deploy.getName());
    }

    /**
     * 流程启动
     * 启动的流程可以在 act_ru_task 中查询
     * 结束的流程可以再 act_hi_taskinst 中查询
     */
    @Test
    public void startTask(){
        String processDefinitionId = findProcessDefinition(key, version);
        //Map可以任意定义 参数
        //Map<String, Object> entity = new HashMap<>();
        //entity.put("code","true");
        //ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, key, entity);
        //开启流程
        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, key);
        System.out.println("数据启动流程ID ---> "+instance.getId());

        Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).active().singleResult();
        System.out.println(task.toString());
    }

    /**
     * 节点处理 通过 / 拒绝
     */
    @Test
    public void complateTask(){
        String InstanceId = "25001";//实例化ID
        Task task = taskService.createTaskQuery().processInstanceId(InstanceId).active().singleResult();
        System.out.println(task.toString());

        Map<String, Object> entity = new HashMap<>();
        entity.put("code","YES");//节点
        taskService.complete(task.getId(),entity);
        System.out.println(task.getName() +" --> 流程执行完成");
    }

    /**
     * 根据实例ID来查询 当前Task任务
     */
    @Test
    public void queryNode(){
        String id = "25001";//实例化ID
        Task task = taskService.createTaskQuery().processInstanceId(id).active().singleResult();
        System.out.println(task.toString());
    }

    @Test
    public void queryMes(){

    }


    /**
     * 删除流程定义
     * act_re_procdef 中 deploymentId 字段
     */
    @Test
    public void deleteProcessDefinition(){
        //使用部署ID，完成删除
        String deploymentId = "17501";
        /*
         * 不带级联的删除
         * 只能删除没有启动的流程，如果流程启动，就会抛出异常
         */
//        processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
//                        .deleteDeployment(deploymentId);
        /*
         * 能级联的删除
         * 能删除启动的流程，会删除和当前规则相关的所有信息，正在执行的信息，也包括历史信息
         */
        processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .deleteDeployment(deploymentId, true);

        System.out.println("删除成功");
    }


    /**
     * 根据传入流程的key和版本号查询流程的ID
     */
    public String findProcessDefinition(String processDefinitionKey, int processVersion) {
        String processDefinitionId = "";
//        System.out.println("===1.processEngine==" + processEngine);
//        System.out.println("===2.repositoryService==" + repositoryService);
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
        System.out.println("processDefinitionId ---> "+processDefinitionId);
        return processDefinitionId;
    }

}
