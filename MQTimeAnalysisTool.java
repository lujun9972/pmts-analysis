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

public class MQTimeAnalysisTool {

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

		if(args.length<2)
			throw new Exception("参数不正确。PMTS日志文件 MQTimeAnalysisTool  [超时时间]15");
		File file = new File(args[0]);

		maxTimeInterv = Long.parseLong(args[1]);
		System.out.println(maxTimeInterv);


		int cnapsCount=0;
		int ibpsCount = 0;
		InputStreamReader read = new InputStreamReader(new FileInputStream(file), "utf-8");
		BufferedReader buffer = new BufferedReader(read);


		String txtBuffer = "";
		String txtLine = null;
		String msgidRecv = "";

		//读取文件数据处理，指定来账报文及回执报文入map
		while((txtLine=buffer.readLine())!=null){
			txtBuffer=txtBuffer+(txtLine);
			//读到一个报文
			if(txtLine.contains("</Document>")){
				//如果是要分析的报文类型ibps.101.001.01/ibps.103.001.01/ibps.105.001.01，并且报文msgid是来账
          //来账报文入mapRecv
          msgidRecv = getMsgID(txtBuffer);
          if(isCome(txtBuffer)){//402602000018开头的是往账
              recvTime = getRecvTime(txtBuffer, df);//接收时间
              proRecvTime = getProRecvTime(txtBuffer, df);//前一节点发送时间
              long MQTimeDiff = (long) (recvTime.getTime() - proRecvTime.getTime())/1000;
              if(MQTimeDiff>=maxTimeInterv){
                  String resultStr = "Msgid: [" + msgidRecv + "] 接收时间:[" + recvTime + "] 前一节点发送时间[" + proRecvTime +  "] MQ耗时=[" + MQTimeDiff + "] ";
                  System.out.println(resultStr);
                  if (isCnaps2(txtBuffer)){
                          cnapsCount++;
                  }else if (isIbps(txtBuffer)){
                          ibpsCount++;
                      }
              }
          }
				txtBuffer = "";
			}
		}
    System.out.println("cnaps2超时:["+cnapsCount+"],ibps超时["+ibpsCount+"]");
	}
    public static String getMsgID(String document)
    {
        try {
            return document.split("</MsgId>")[0].split("<MsgId>")[1];
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            return "";
        }

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

    public static boolean isCome(String document)
    {
        // !msgidRecv.substring(0, 12).equals("402602000018")
        return !document.contains("首选发送队列名:[MSGTO5810B_1]") ||
            document.contains("首选发送队列名:[MSGTO5810A_1]") ||
            document.contains("首选发送队列名:[MSGTO5810B_2]") ||
            document.contains("首选发送队列名:[MSGTO5810A_2]") ||
            document.contains("首选发送队列名:[MSGTO0020A_1]") ||
            document.contains("首选发送队列名:[MSGTO0020A_2]") ||
            document.contains("首选发送队列名:[MSGTO0020B_1]") ||
            document.contains("首选发送队列名:[MSGTO0020B_2]");
    }

    public static boolean isCnaps2(String document)
    {
        return document.contains("首选发送队列名:[MSGTOCNAPA");
    }

    public static boolean isIbps(String document)
    {
        return document.contains("首选发送队列名:[MSGTOIBPSA");
    }

}
