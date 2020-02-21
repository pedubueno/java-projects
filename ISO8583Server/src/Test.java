import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.util.Bcd;

public class Test
{

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static final Logger log = LogManager.getLogger(Test.class);

	public static void main(String[] args)
	{
		try
		{			
			testTEFMsg();
			testPOSMsg();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static void testPOSMsg() throws Exception
	{
		String hexMsg = "60075100070200B2380700A1E182AA020000000000000000300000000000800801021730270012041430160102005100000088040010375201328234531177D23122012358603555371F3030303030333836323535303030303030303030333730343930574C33393539383134312020202020202020202020202020202020202020202020202020202020200020F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0098601419F2701809F10120114A50003020000000000000000000000FF9F3704ACFE17289F3602018F950580000080009A032001029C01009F02060000000080085F2A020986820218009F1A0200769F260893E733B2D928ED415F3401009F3303E0F8C89F34034103029F12074372656469746F5F201A45524E45535420474F554C4152542020202020202020202020200013303130323030303033333731380023303530303030303030303030313032303130313032343700103030303030303030323000113030303430303430303034303031572E393138322020202020";

		
		MessageFactory mf = new MessageFactory<>();
		mf.setCharacterEncoding("UTF-8");
		mf.setConfigPath("config_pos.xml");
		mf.setBinaryHeader(true);
		mf.setBinaryFields(true);
		
		
		IsoMessage tst = mf.parseMessage(hexToBytes(hexMsg), 5, true);
		
		System.out.println("MSG OUT=" + bytesToHex(tst.writeData()));
		
		System.out.println("END");	
	}

	private static void testTEFMsg() throws Exception
	{
		
		
		String BCDAmount = "000000000150";
		byte[] buf = new byte[6];
		
		Bcd.encode(BCDAmount, buf);
		
		
		String hexBcdValue = bytesToHex(buf);
		
		String hexMsg = "30323030B238040021E08044000000000000020030303330303030303030303030303130393031323136313533383536303030303031313533383536313231363032313337353434373331585858585858585858583D323231323130313033313739303735343030313030303130303030303032323030303030303030303339353933323037353132303231343230303030393939392F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F2F39383630303854303338353430343031323036303030303553504C495433343530363031303030343665623230363032303030343433383030363033303030343162626630363130303330313036313130303135383533393834373539383334373334303631323030303631343837303330363230303038303036323130303035303030303130363232303030353338353535303632333030313744455343524943414F2050524F44555431303632353030303930303539333435303030363236303030343235393930363230303038303036323130303035583030303230363232303030353338353636303632333030313744455343524943414F2050524F44555432303632353030303930303539333435303030363236303030343235393930363230303038303036323130303035583030303330363232303030353338353737303632333030313744455343524943414F2050524F445554333036323530303039303035393334353030303632363030303432353939";
		String hexLength = "0225";
		
		
		String TEFMsg = "0200B238060021E08264200000000000020000300000000000120012261333240014711033241226051000375181480008747960=16052015919215490369001001001010000000003959327200011611000289                        9862629F2701809F10120114A50003020000000000000000000000FF9F37046F7899009F36020262950542400080009A031912269C01009F02060000000012005F2A020986820258009F1A0200769F2608AEE6B7A1A39E8D7C5F3401009F3303E0F8C89F34034103028407A00000000410109F0607A00000000410109F090200025F2A020986008T03854041660060006GERTEC00610042.9900620113.01.14.20600630042.030064016001.12 160914   00650113.01.14.20600660113.01.14.2060067003OKI0068013PPC-930;192MB00690171201.10300.0000010540001001N0030001S035100412340537008000000000600005SPLIT01608060100366eb2412c-165a-41cd-b1d9-76c575d70a28060200366d2e4380-d8a3-4ccb-9138-c289182818a30610021606110015853984759834734061200061487030620008206210005X0001062200053859506230019DESCRICAO PRODUTO 1062500090059345000626000425990620008106210005X0002062200051230006230019DESCRICAO PRODUTO 206250009001012300062600035000610028806110015778899112233445061200061122330620007906210005X00030622000334506230019DESCRICAO PRODUTO 306250009001122200062600032000620006806210005X00040622000612345606230019DESCRICAO PRODUTO 406260006.�...=0620008006210005X000506220004678906230019DESCRICAO PRODUTO 50625000900334001106260003543";

		MessageFactory mf = new MessageFactory<>();
		mf.setCharacterEncoding("UTF-8");
		mf.setConfigPath("config_tef.xml");
		
		log.info(mf.isBinaryHeader());
		
		IsoMessage tst = mf.newMessage(0x200);
		System.out.println(tst.isBinaryHeader());
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
		 
		
		IsoMessage tefMsg = mf.parseMessage(TEFMsg.getBytes(), 0);
		System.out.println(tefMsg.getField(4).getValue());
		System.out.println(tefMsg.getField(41).getValue());
		System.out.println(tefMsg.getField(42).getValue());
		System.out.println(tefMsg.getField(58).getValue());
		System.out.println(tefMsg.getField(119).getValue());
		//System.out.println(tefMsg.getField(120).getValue());
		
		//build response
		IsoMessage tefResponseMessage = mf.createResponse(tefMsg);
		tefResponseMessage.setField(127, new IsoValue<String>(IsoType.LLLLVAR,"123456789"));
		
		tefResponseMessage.removeFields(2, 35, 41, 42, 45, 58, 119);
		
		log.info("CURURU MESSAGE: " + tefResponseMessage.debugString());

		
		sdf.format(new Date());
		
		
		IsoMessage isoMessage = mf.newMessage(0x200);
		isoMessage.setBinaryHeader(true);
		isoMessage.setBinaryFields(true);
		isoMessage.setField(2, new IsoValue<String>(IsoType.LLBCDBIN, "5447311234567890"));
		isoMessage.setField(3, new IsoValue<String>(IsoType.NUMERIC, "003000", 6));
		isoMessage.setField(4, new IsoValue<Double>(IsoType.AMOUNT, 10.90));
		isoMessage.setField(7, new IsoValue<String>(IsoType.DATE10, sdf.format(new Date())));
		isoMessage.setField(11, new IsoValue<Integer>(IsoType.NUMERIC, 1, 6));
		isoMessage.setField(12, new IsoValue<String>(IsoType.TIME, new SimpleDateFormat("HHmmss").format(new Date())));
		isoMessage.setField(13, new IsoValue<String>(IsoType.DATE4, new SimpleDateFormat("MMdd").format(new Date())));
		isoMessage.setField(22, new IsoValue<String>(IsoType.NUMERIC, "021", 3));
		isoMessage.setField(35, new IsoValue<String>(IsoType.LLVAR, "544731XXXXXXXXXX=22121010317907540010"));
		isoMessage.setField(40, new IsoValue<String>(IsoType.ALPHA, "001", 3));
		isoMessage.setField(41, new IsoValue<String>(IsoType.ALPHA, "00000022", 8));
		isoMessage.setField(42, new IsoValue<String>(IsoType.ALPHA, "000000000395932", 15));
		isoMessage.setField(43, new IsoValue<String>(IsoType.ALPHA, "075120214200009999//////////////////////", 40));
		isoMessage.setField(49, new IsoValue<Integer>(IsoType.NUMERIC, 986, 3));
		isoMessage.setField(58, new IsoValue<String>(IsoType.LLLVAR, "T0385404"));
		isoMessage.setField(62, new IsoValue<String>(IsoType.LLLVAR, "0600005SPLIT"));
		isoMessage.setField(119, new IsoValue<String>(IsoType.LLLVAR, "060100046eb2060200044380060300041bbf061003010611001585398475983473406120006148703062000800621000500001062200053855506230017DESCRICAO PRODUT1062500090059345000626000425990620008006210005X0002062200053856606230017DESCRICAO PRODUT2062500090059345000626000425990620008006210005X0003062200053857706230017DESCRICAO PRODUT306250009005934500062600042599"));
		
		System.out.println("BUILT MESSAGE: " + isoMessage.debugString());
		System.out.println("HEX DEBUG STRING MESSAGE: " + bytesToHex(isoMessage.debugString().getBytes()));
		System.out.println("HEX DUMP MESSAGE " + bytesToHex(isoMessage.writeData()));
		
		
		IsoMessage isoParsedMessage = mf.parseMessage(isoMessage.debugString().getBytes(), 6);
		

		System.out.println("PARSE MESSAGE: " + isoParsedMessage.debugString());
		
		
		System.out.println("Tamanho int header hex: " + get2byteBufferSize(hexToBytes(hexLength)));

		
	}

	public static String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexToBytes(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private static byte[] new2byteHeaderPacket(byte[] data)
	{
		int len = data.length;
		byte[] buf = new byte[len + 2];

		buf[0] = ((byte) (len >> 8 & 0xFF));
		buf[1] = ((byte) (len & 0xFF));

		System.arraycopy(data, 0, buf, 2, len);

		return buf;
	}

	private static int get2byteBufferSize(byte[] header_buf)
	{
		return (header_buf[0] & 0xFF) << 8 | header_buf[1] & 0xFF;
	}
}