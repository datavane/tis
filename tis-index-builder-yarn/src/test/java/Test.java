import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;



/**
 * 
 */
public class Test {
	public static void main(String[] args) {

		//System.out.println(URLEncoder.encode("pt=2019092714(\\d{4})"));
		
	 System.out.println(	URLEncoder.encode(	"tis%E4%BB%8B%E7%BB%8D%E4%B8%8E%E6%95%B4%E4%BD%93%E6%9E%B6%E6%9E%84") );
	 
	 System.out.println(	URLDecoder.decode( "#tis%25E4%25BB%258B%25E7%25BB%258D%25E4%25B8%258E%25E6%2595%25B4%25E4%25BD%2593%25E6%259E%25B6%25E6%259E%2584"));
	}
}
