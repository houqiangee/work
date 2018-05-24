package business.stock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.framework.layer.BPO;
import com.framework.layer.Controller;
import com.framework.util.DataObject;


public class StockController extends Controller{
	
	/**
	 * 查某个股票的k线
	 * @author hq
	 */
	public ModelAndView showOneStockK(HttpServletRequest request,
			HttpServletResponse response, DataObject para) throws Exception {
		BPO nbpo=this.newBPO(StockBPO.class);
		DataObject vdo=nbpo.doMethod("showOneStockK", para,this.getUser(request));
		return this.writeMsg(response, vdo.toJSON());
	}
	
}