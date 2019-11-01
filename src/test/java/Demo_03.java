import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author CRC
 * @date 2019-10-11
 * 历史查询
 * 参考   https://blog.csdn.net/zjx86320/article/details/50363544
 */
public class Demo_03 {

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


    /**查询历史活动
     * act_hi_actinst 表中查
     * HistoricActivityInstance（所有流程）和 HistoricTaskInstance（人物执行相关）
     */
    @Test
    public void findHisActivitiList(){
        String processInstanceId = "5001";
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByProcessInstanceId().asc()
                .list();
        if(list != null && list.size()>0){
            for(HistoricActivityInstance hai : list){
                System.out.println(hai.getId()+""+hai.getActivityName());
            }
        }
    }

    /**
     * 查询历史任务
     * act_hi_taskinst 表中查
     */
    @Test
    public void findHisTaskList(){
        String processInstanceId = "5001";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).orderByProcessInstanceId().asc()
                .list();
        if(list!=null && list.size()>0){
            System.out.println("ID      流程名字        流程Key");
            for(HistoricTaskInstance hti:list){
                System.out.println(hti.getId()+"    "+hti.getName()+"   "+hti.getTaskDefinitionKey());
            }
        }
    }

    /**
     * 查询历史流程变量
     * act_hi_varinst 表中查
     */
    @Test
    public void findHisVariablesList(){
        String processInstanceId = "5006";
        List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if(list != null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                System.out.println(hvi.getId()+"    "+hvi.getVariableName()+"	"+hvi.getValue());
            }
        }
    }




    /**
     * 当某个任务结束以后,在任务表act_ru_task中的记录会被删除 ,
     * 但是act_hi_taskinst记录的endtime会加上 ,所以我们可以从这个表查询我们的任务执行历史记录
     */
    @Test
    public void testQueryHistoryTask() {
        String assignee = "vicky";
        List<HistoricTaskInstance> historyTaskList = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .list();
        if (historyTaskList != null && historyTaskList.size() > 0) {
            for (HistoricTaskInstance hisTask : historyTaskList) {
                System.out.println("流程定义ID:" + hisTask.getProcessDefinitionId());
                System.out.println("流程实例ID:" + hisTask.getProcessInstanceId());
                System.out.println("执行对象ID:" + hisTask.getExecutionId());
                System.out.println("历史任务ID:" + hisTask.getId());//任务ID:12502
                System.out.println("历史任务名称:" + hisTask.getName());
                System.out.println("历史任务的结束时间:" + hisTask.getEndTime());
                System.out.println("---------------------");
            }
        }
    }

    /**
     * 当某个流程实例执行完以后以后,在流程实例表表act_ru_execution中的记录会被删除 ,
     * 但是act_hi_procinst表中记录的endtime会加上 ,所以我们可以从这个表查询我们的历史流程实例记录
     */
    @Test
    public void testQueryHistoryProcessInstance() {
        String processInstanceId = "130001";
        HistoricProcessInstance hisProcessInst = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (hisProcessInst != null) {
            System.out.println("流程定义ID:" + hisProcessInst.getProcessDefinitionId());
            System.out.println("执行对象ID:" + hisProcessInst.getId());
            System.out.println("---------------------");
        }
    }




}
