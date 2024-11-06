package com.luckyframe.project.testmanagmt.projectProtocolTemplate.controller;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.luckyframe.common.utils.security.PermissionUtils;
import com.luckyframe.framework.aspectj.lang.annotation.Log;
import com.luckyframe.framework.aspectj.lang.enums.BusinessType;
import com.luckyframe.framework.web.controller.BaseController;
import com.luckyframe.framework.web.domain.AjaxResult;
import com.luckyframe.project.testmanagmt.projectProtocolTemplate.domain.ProjectProtocolTemplate;
import com.luckyframe.project.testmanagmt.projectProtocolTemplate.domain.ProjectTemplateParams;
import com.luckyframe.project.testmanagmt.projectProtocolTemplate.service.IProjectProtocolTemplateService;
import com.luckyframe.project.testmanagmt.projectProtocolTemplate.service.IProjectTemplateParamsService;

/**
 * 模板参数管理 信息操作处理
 * 
 * @author luckyframe
 * @date 2019-03-04
 */
@Controller
@RequestMapping("/testmanagmt/projectTemplateParams")
public class ProjectTemplateParamsController extends BaseController
{
	@Autowired
	private IProjectTemplateParamsService projectTemplateParamsService;

	@Autowired
	private IProjectProtocolTemplateService projectProtocolTemplateService;
	
	/**
	 * 修改模板参数
	 * @param templateId 模板ID
	 * @param mmap 返回数据模型
	 * @author Seagull
	 * @date 2019年3月7日
	 */
	@GetMapping("/editParams/{templateId}")
	public String edit(@PathVariable Integer templateId, ModelMap mmap)
	{
		String paramsType="raw";
		ProjectProtocolTemplate projectProtocolTemplate=projectProtocolTemplateService.selectProjectProtocolTemplateById(templateId);
		ProjectTemplateParams projectTemplateParams = new ProjectTemplateParams();
		ProjectTemplateParams jsonParam = new ProjectTemplateParams();		
		projectTemplateParams.setTemplateId(templateId);
		List<ProjectTemplateParams> templateParams = projectTemplateParamsService.selectProjectTemplateParamsList(projectTemplateParams);
		/*判断是否无参数，设置默认值 */
		if(templateParams.size()==0){
			projectTemplateParams.setParamsId(0);
			projectTemplateParams.setParamName("");
			projectTemplateParams.setParamValue("");
			projectTemplateParams.setParamType(0);
			templateParams.add(projectTemplateParams);
			
			/*JSON单文本格式对象，重新设置参数 */
			jsonParam.setParamsId(0);
			jsonParam.setTemplateId(templateId);
			jsonParam.setParamName("_forTextJson");
			jsonParam.setParamValue("");
			jsonParam.setParamType(0);
		}else if(templateParams.size()==1&&"_forTextJson".equals(templateParams.getFirst().getParamName())){
			/*判断是否是RAW JSON单文本格式 */
			jsonParam.setParamsId(templateParams.getFirst().getParamsId());
			jsonParam.setTemplateId(templateId);
			jsonParam.setParamName("_forTextJson");
			jsonParam.setParamType(0);
			jsonParam.setParamValue(templateParams.getFirst().getParamValue());
			
			projectTemplateParams=templateParams.getFirst();
			projectTemplateParams.setParamsId(0);
			projectTemplateParams.setParamName("");
			projectTemplateParams.setParamValue("");
			projectTemplateParams.setParamType(0);
		}else{
			/*判断是否是form格式 */
			paramsType="form";
			
			/*JSON单文本格式对象，重新设置参数 */
			jsonParam.setParamsId(0);
			jsonParam.setTemplateId(templateId);
			jsonParam.setParamName("_forTextJson");
			jsonParam.setParamValue("");
			jsonParam.setParamType(1);
		}
		
		mmap.put("ptemplate", projectProtocolTemplate);
		mmap.put("templateParams", templateParams);
		mmap.put("paramsType", paramsType);
		mmap.put("jsonParam", jsonParam);
	    return "testmanagmt/projectProtocolTemplate/projectTemplateParams";
	}
	
	/**
	 * 修改保存模板参数管理
	 */
	@RequiresPermissions("testmanagmt:projectProtocolTemplate:edit")
	@Log(title = "模板参数管理", businessType = BusinessType.UPDATE)
	@PostMapping(value = "/editSave",consumes="application/json")
	@ResponseBody
	public AjaxResult editSave(@RequestBody List<ProjectTemplateParams> projectTemplateParams)
	{		
		if(!PermissionUtils.isProjectPermsPassByProjectId(projectProtocolTemplateService
				.selectProjectProtocolTemplateById(projectTemplateParams.getFirst().getTemplateId()).getProjectId())){
			return error("没有此项目保存模板参数权限");
		}
		
		int result=0;
		projectTemplateParamsService.deleteProjectTemplateParamsByIds(projectTemplateParams);
		for(ProjectTemplateParams projectTemplateParam:projectTemplateParams){
			result=result+projectTemplateParamsService.insertProjectTemplateParams(projectTemplateParam);
		}
		return toAjax(result);
	}
	
}
