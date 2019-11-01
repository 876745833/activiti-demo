import org.activiti.engine.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author CRC
 * @date 2019-10-12
 * 参考   https://blog.csdn.net/qq_27467601/article/details/54694773
 */
public class RollBack {

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

    @Test
    public void test(){
        rollBackToAssignWoekFlow("5001","usertask2");
    }

    /**
     * 撤回
     * @param processInstanceId
     * @param destTaskkey
     */
    public void rollBackToAssignWoekFlow(String processInstanceId,String destTaskkey){
        // 取得当前任务.当前任务节点
        destTaskkey ="usertask2";
       // HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        Map<String, Object> variables;
        ExecutionEntity entity = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(processInstanceId).singleResult();
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity)((RepositoryServiceImpl)repositoryService)
          .getDeployedProcessDefinition(entity.getProcessDefinitionId());
        variables = entity.getProcessVariables();
        //当前活动环节
        ActivityImpl currActivityImpl = definition.findActivity(entity.getActivityId());
        //目标活动节点
        ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition).findActivity(destTaskkey);
        if(currActivityImpl !=null){
            //所有的出口集合
            List<PvmTransition> pvmTransitions = currActivityImpl.getOutgoingTransitions();
            List<PvmTransition> oriPvmTransitions = new ArrayList<PvmTransition>();
            for(PvmTransition transition : pvmTransitions){
                oriPvmTransitions.add(transition);
            }
            //清除所有出口
            pvmTransitions.clear();
            //建立新的出口
            List<TransitionImpl> transitionImpls = new ArrayList<TransitionImpl>();
            TransitionImpl tImpl = currActivityImpl.createOutgoingTransition();
            tImpl.setDestination(nextActivityImpl);
            transitionImpls.add(tImpl);

            List<Task> list = taskService.createTaskQuery().processInstanceId(entity.getProcessInstanceId())
                    .taskDefinitionKey(entity.getActivityId()).list();
            for(Task task:list){
                taskService.complete(task.getId(), variables);
                historyService.deleteHistoricTaskInstance(task.getId());
            }

            for(TransitionImpl transitionImpl:transitionImpls){
                currActivityImpl.getOutgoingTransitions().remove(transitionImpl);
            }

            for(PvmTransition pvmTransition:oriPvmTransitions){
                pvmTransitions.add(pvmTransition);
            }
        }
    }
    

}
