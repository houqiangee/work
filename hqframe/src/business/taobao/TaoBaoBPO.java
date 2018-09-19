package business.taobao;

import com.framework.layer.BPO;
import com.framework.util.DataObject;
import com.framework.util.DataStore;


public class TaoBaoBPO extends BPO{
	
	/**
	 * ����𰸲�ȡ�¸���֤��
	 * @author hq
	 */
	public DataObject saveAnswerAndGetNewImg(DataObject para) throws Exception {
		int rcount=para.getInt("rcount");
		DataObject vdo=new DataObject();
		sql.setSql("select imgurl from stock.taobao_yzm where answer is null or answer='' limit "+rcount);
		DataStore vds=sql.executeQuery();
		vdo.put("vds", vds);
		return vdo;
	}
	
}