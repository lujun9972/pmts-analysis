import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TimeOutAnalysisTool {

	public static HashMap<String,String> mapRecv = new HashMap<String,String>();
	public static HashMap<String,String> mapSend = new HashMap<String,String>();
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Date recvTime = null;
		Date proRecvTime = null;
		Date sendTime = null;
		long maxTimeInterv = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//String bwIden1 = "XMLibps.101.001.01";
		//String bwIden2 = "XMLibps.102.001.01";		
		
		if(args.length<2)
			throw new Exception("参数不正确。TimeOutAnalysisTool [报文类型]ibps.101.001.01 [超时时间]15");
		String bwIden1 = "";
		String bwIden2 = "";
		File file = new File(args[0]);
		
		bwIden1 = args[1];
		maxTimeInterv = Long.parseLong(args[2]);
		System.out.println(bwIden1);
		System.out.println(maxTimeInterv);
		
		if(bwIden1.equalsIgnoreCase("ibps.101.001.01")){
			bwIden1 = "XMLibps.101.001.01";
			bwIden2 = "XMLibps.102.001.01";
		}else if(bwIden1.equalsIgnoreCase("ibps.103.001.01")){
			bwIden1 = "XMLibps.103.001.01";
			bwIden2 = "XMLibps.104.001.01";
		}else if(bwIden1.equalsIgnoreCase("ibps.105.001.01")){
			bwIden1 = "XMLibps.105.001.01";
			bwIden2 = "XMLibps.106.001.01";
		}else{
			throw new Exception("报文类型不正确，ibps.101.001.01/ibps.103.001.01/ibps.105.001.01");
		}

		int count=0;
		int countSend = 0;
		InputStreamReader read = new InputStreamReader(new FileInputStream(file), "utf-8");
		BufferedReader buffer = new BufferedReader(read);
		
		FileWriter fw = new FileWriter("result_pmts1_"+bwIden1+".txt");
		
		
		String txtBuffer = "";
		String txtLine = null;
		String msgidRecv = "";
		
		//读取文件数据处理，指定来账报文及回执报文入map
		while((txtLine=buffer.readLine())!=null){
			txtBuffer=txtBuffer+(txtLine);
			//读到一个报文
			if(txtLine.contains("</Document>")){
				//如果是要分析的报文类型ibps.101.001.01/ibps.103.001.01/ibps.105.001.01，并且报文msgid是来账
				if(txtBuffer.contains(bwIden1)){
					//来账报文入mapRecv
					msgidRecv = getMsgID(txtBuffer);
					if(!msgidRecv.substring(0, 12).equals("402602000018")){//402602000018开头的是往账
						mapRecv.put(msgidRecv, txtBuffer);
					}else{
						countSend++;
					}
				}else if(txtBuffer.contains(bwIden2)){
					//回执报文入mapSend
					msgidRecv = getOrgMsgID(txtBuffer);
					mapSend.put(msgidRecv, txtBuffer);
				}
				txtBuffer = "";
			}
		}
		//遍历map，计算耗时
		Iterator<Map.Entry<String,String>> iter = mapRecv.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> messageRecv = iter.next();
			recvTime = getRecvTime(messageRecv.getValue(),df);//接收时间
			proRecvTime = getProRecvTime(messageRecv.getValue(),df);//前一节点发送时间
			if(mapSend.get(messageRecv.getKey())==null)//没有回执报文
					continue;
			sendTime = getRecvTime(mapSend.get(messageRecv.getKey()),df);//回执时间
			
			long timeDiffIbps = (long) (sendTime.getTime() - recvTime.getTime())/1000;
			long timeDiffTotal = (long) (sendTime.getTime() - proRecvTime.getTime())/1000;
			String resultStr = "";
			resultStr = "Msgid: [" + messageRecv.getKey() + "] 接收时间:[" + recvTime + "] 前一节点发送时间[" + proRecvTime 
							+ "] 回执时间:[" + sendTime + "] 行内耗时=[" + timeDiffIbps + "] 总耗时=[" + timeDiffTotal + "] 笔数=" + (count+1);
			System.out.println(resultStr);
			if(timeDiffTotal>=maxTimeInterv){
				fw.write(resultStr+"\r\n");
			}
			count++;
		}
		fw.write("recv_total="+count+"\r\n");

		fw.write("send_total="+countSend);
		fw.close();
		
			
	}
	
	public static String getMsgID(String document)
	{
		return document.split("</MsgId>")[0].split("<MsgId>")[1];
	}
	
	public static String getOrgMsgID(String document)
	{
		return document.split("</OrgnlMsgId>")[0].split("<OrgnlMsgId>")[1];
	}

	public static Date getRecvTime(String document,DateFormat df) throws ParseException 
	{
		//System.out.println("1======"+document.split("Level 0 PMTSMSGHDL:")[0].split("\\[")[1].substring(0, 19));
		Date d = df.parse(document.split("Level 0 PMTSMSGHDL:")[0].split("\\[")[1].substring(0, 19));
		return d;
	}
	
	public static Date getProRecvTime(String document,DateFormat df) throws ParseException
	{
		//System.out.println("2======"+document.split("Level 0 PMTSMSGHDL:")[1].split("]")[0].split("\\[")[1].substring(0, 19));
		Date d = df.parse(document.split("Level 0 PMTSMSGHDL:")[1].split("]")[0].split("\\[")[1].substring(0, 19));
		return d;
	}

}
