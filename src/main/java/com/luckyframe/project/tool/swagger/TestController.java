package com.luckyframe.project.tool.swagger;

import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.luckyframe.framework.web.controller.BaseController;
import com.luckyframe.framework.web.domain.AjaxResult;

/**
 * swagger 测试方法
 * 
 * @author ruoyi
 */
@Tag(name = "用户信息管理")
@RestController
@RequestMapping("/test/*")
public class TestController extends BaseController
{
    private final static List<Test> testList = new ArrayList<>();
    static {
        testList.add(new Test("1", "admin", "admin123"));
        testList.add(new Test("2", "ry", "admin123"));
    }

    @Operation(summary = "获取列表")
    @GetMapping("list")
    public List<Test> testList()
    {
        return testList;
    }

    @Operation(summary = "新增用户")
    @PostMapping("save")
    public AjaxResult save(Test test)
    {
        return testList.add(test) ? success() : error();
    }

    @Operation(summary = "更新用户")
    @Parameter(name = "Test", description = "单个用户信息")
    @PutMapping("update")
    public AjaxResult update(Test test)
    {
        return testList.remove(test) && testList.add(test) ? success() : error();
    }

    @Operation(summary = "删除用户")
    @Parameter(name = "Tests", description = "单个用户信息")
    @DeleteMapping("delete")
    public AjaxResult delete(Test test)
    {
        return testList.remove(test) ? success() : error();
    }
}

class Test
{
    private String userId;
    private String username;
    private String password;

    public Test()
    {

    }

    public Test(String userId, String username, String password)
    {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Test test = (Test) o;

        return userId != null ? userId.equals(test.userId) : test.userId == null;
    }

    @Override
    public int hashCode()
    {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
