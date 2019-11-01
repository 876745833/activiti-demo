import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.impl.cfg.IdGenerator;

public class TaskIdGenerator implements IdGenerator {
	
	private static TaskIdGenerator tp=new TaskIdGenerator();
	private static int number=0;
	private static String ip="";
	
	private TaskIdGenerator(){}
	
	public static TaskIdGenerator getInstance(){
		 if(tp==null){
			tp=new TaskIdGenerator();
		 }
		 return tp;
	 }
	
    public String getNextId() {
       return getTrace();
    }
	  
    /**
	 * 交易流水的生成
	 * data（交易流水）=1（服务器IP）+YYMMDD（日期）+111111(6为时间戳)+11111(生成数字)
	 */
	public synchronized String getTrace(){
		String data=null;
		String IP = null;
		String str= null;
		number++;
		try {
			if(ip.equals("")) {
				ip=InetAddress.getLocalHost().getHostAddress().split("[.]")[3];
			}
			IP=ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		String dateStr=sdf.format(new Date());
		dateStr = dateStr.substring(2, dateStr.length());
		str="00000"+number;
		str = str.substring(str.length()-5);
		data=IP+dateStr+str;
		if(number>99999){
			number=0;
		}
		return data;
	}
	
	public static void main(String[] args) throws UnknownHostException {
		System.out.println();
	}
	
}