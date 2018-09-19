package business.taobao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import business.stock.StockBPO;

import com.framework.layer.BPO;
import com.framework.layer.Controller;
import com.framework.util.DataObject;


public class TaoBaoController extends Controller{
	
	/**
	 * 保存答案并取下个验证码
	 * @author hq
	 */
	public ModelAndView saveAnswerAndGetNewImg(HttpServletRequest request,
			HttpServletResponse response, DataObject para) throws Exception {
		BPO nbpo=this.newBPO(TaoBaoBPO.class);
		DataObject vdo=nbpo.doMethod("saveAnswerAndGetNewImg", para,this.getUser(request));
		return this.writeMsg(response, vdo.toJSON());
	}
	
}