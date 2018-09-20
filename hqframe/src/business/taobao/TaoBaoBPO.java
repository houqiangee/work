package business.taobao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.framework.layer.BPO;
import com.framework.util.DataObject;
import com.framework.util.DataStore;
import com.framework.util.Transaction;
import com.framework.util.TransactionManager;


public class TaoBaoBPO extends BPO{
	
	/**
	 * 保存答案并取下个验证码
	 * @author hq
	 */
	public DataObject saveAnswerAndGetNewImg(DataObject para) throws Exception {
		String data=para.getString("data","[]");
		JSONArray jar = JSONArray.fromObject(data);
		Transaction tm=TransactionManager.getTransaction();tm.begin();
		for(int i=0;i<jar.size();i++){
		    JSONObject job = jar.getJSONObject(i);
		    String imgurl=job.getString("imgurl");
		    String answer=job.getString("answer");
		    if(imgurl==null || "".equals(imgurl) || answer==null ||"".equals(answer) ){
		    	continue;
		    }
		    sql.setSql("update stock.taobao_yzm set answer=? where imgurl=? ");
		    sql.setString(1, answer);
		    sql.setString(2, imgurl);
		    sql.executeUpdate();
		    tm.commit();
		} 
		
		int rcount=para.getInt("rcount");
		DataObject vdo=new DataObject();
		sql.setSql("select imgurl from stock.taobao_yzm where answer is null or answer='' limit "+rcount);
		DataStore vds=sql.executeQuery();
		vdo.put("vds", vds.toJSON());
		return vdo;
	}
	
}