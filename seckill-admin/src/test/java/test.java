import com.xiaoliu.seckill.MainApplication;
import com.xiaoliu.seckill.model.SeckillAdmin;
import com.xiaoliu.seckill.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SpringBootTest(classes = MainApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class test {

    @Autowired
    private AdminService adminService;

    @Test
    public void test01() throws ParseException {
    /*    SeckillAdmin seckillAdmin = new SeckillAdmin();
        List<SeckillAdmin> list = adminService.list(seckillAdmin);
        for (int i = 0; i < list.size(); i++) {
            SeckillAdmin admin = list.get(i);
            Date createTime = admin.getCreateTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss 'CST' yyyy", Locale.ENGLISH);
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = dateFormat1.format(dateFormat.parse(createTime.toString()));
            Date parse = dateFormat1.parse(format);
            admin.setCreateTime(parse);

            System.out.println(parse.toString());
        }*/
    }

}
