package com.luckyframe.project.testmanagmt.projectCase.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.luckyframe.common.utils.poi.ExcelUtil;
import com.luckyframe.project.common.POUtil;
import com.luckyframe.project.testmanagmt.projectCaseModule.domain.ProjectCaseModule;
import com.luckyframe.project.testmanagmt.projectPageDetail.domain.ProjectPageDetail;
import com.luckyframe.project.testmanagmt.projectPageDetail.service.IProjectPageDetailService;
import com.luckyframe.project.testmanagmt.projectPageObject.domain.ProjectPageObject;
import com.luckyframe.project.testmanagmt.projectPageObject.service.IProjectPageObjectService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.luckyframe.common.utils.StringUtils;
import com.luckyframe.common.utils.security.PermissionUtils;
import com.luckyframe.framework.aspectj.lang.annotation.Log;
import com.luckyframe.framework.aspectj.lang.enums.BusinessType;
import com.luckyframe.framework.web.controller.BaseController;
import com.luckyframe.framework.web.domain.AjaxResult;
import com.luckyframe.project.testmanagmt.projectCase.domain.ProjectCase;
import com.luckyframe.project.testmanagmt.projectCase.domain.ProjectCaseSteps;
import com.luckyframe.project.testmanagmt.projectCase.service.IProjectCaseService;
import com.luckyframe.project.testmanagmt.projectCase.service.IProjectCaseStepsService;

import cn.hutool.core.util.StrUtil;

/**
 * 测试用例步骤管理 信息操作处理
 *
 * @author luckyframe
 * @date 2019-02-26
 */
@Controller
@RequestMapping("/testmanagmt/projectCaseSteps")
public class ProjectCaseStepsController extends BaseController {
    @Autowired
    private IProjectCaseStepsService projectCaseStepsService;

    @Autowired
    private IProjectCaseService projectCaseService;
    @Autowired
    private IProjectPageObjectService projectPageObjectService;

    @Autowired
    private IProjectPageDetailService projectPageDetailService;

    /**
     * 修改测试用例步骤管理
     */
    @GetMapping("/edit/{caseId}")
    public String edit(@PathVariable("caseId") Integer caseId, ModelMap mmap) {
        ProjectCase projectCase = projectCaseService.selectProjectCaseById(caseId);
        ProjectCaseSteps projectCaseSteps = new ProjectCaseSteps();
        projectCaseSteps.setCaseId(caseId);
        List<ProjectCaseSteps> stepsList = projectCaseStepsService.selectProjectCaseStepsList(projectCaseSteps);

        if (stepsList.size() == 0) {
            projectCaseSteps.setAction("");
            projectCaseSteps.setExpectedResult("");
            projectCaseSteps.setExtend("");
            projectCaseSteps.setProjectId(projectCase.getProjectId());
            projectCaseSteps.setStepId(0);
            projectCaseSteps.setStepOperation("");
            projectCaseSteps.setStepParameters("");
            projectCaseSteps.setStepPath("");
            projectCaseSteps.setStepSerialNumber(1);
            projectCaseSteps.setStepType(projectCase.getCaseType());
            stepsList.add(projectCaseSteps);
        }

        for (ProjectCaseSteps steps : stepsList) {
            if (POUtil.isPO(steps)) {
                {
                    try {
                        //页面名称
                        int pageId = Integer.parseInt(steps.getStepPath().split("\\.")[0]);
                        //元素名称
                        int elementId = Integer.parseInt(steps.getStepPath().split("\\.")[1]);
                        ProjectPageObject tmp = new ProjectPageObject();
                        tmp.setProjectId(steps.getProjectId());
                        tmp.setPageId(pageId);
                        ProjectPageObject projectPageObject = projectPageObjectService.selectProjectPageObjectList(tmp).getFirst();
                        String pageName = projectPageObject.getPageName();
                        projectPageObject.setProjectId(steps.getProjectId());
                        ProjectPageDetail projectPageDetail = projectPageDetailService.selectProjectPageDetailById(elementId);
                        String element = projectPageDetail.getElement();
                        steps.setStepPath(pageName + "." + element);
                    } catch (Exception e) {

                    }
                    }
                }
                if (StrUtil.isBlank(steps.getStepRemark())) {
                    steps.setStepRemark("备注");
                }
                if (StrUtil.isBlank(steps.getExtend())) {
                    steps.setExtend("");
                }
                if (StrUtil.isBlank(steps.getAction())) {
                    steps.setAction("");
                }
                if (StrUtil.isBlank(steps.getStepParameters())) {
                    steps.setStepParameters("");
                }
            }

            mmap.put("stepsList", stepsList);
            mmap.put("projectCase", projectCase);
            return "testmanagmt/projectCase/projectCaseSteps";
        }

        /**
         * 修改保存测试用例步骤管理
         */
        @RequiresPermissions("testmanagmt:projectCase:edit")
        @Log(title = "测试用例步骤管理", businessType = BusinessType.UPDATE)
        @RequestMapping(value = "/editSave", method = RequestMethod.POST, consumes = "application/json")
        @ResponseBody
        public AjaxResult editSave (@RequestBody List < ProjectCaseSteps > listSteps) {
            if (!PermissionUtils.isProjectPermsPassByProjectId(listSteps.getFirst().getProjectId())) {
                return error("没有此项目编辑用例步骤权限");
            }

            int result = 0;
            projectCaseStepsService.deleteProjectCaseStepsByIds(listSteps);
            int stepSerialNumber = 1;
            for (ProjectCaseSteps projectCaseSteps : listSteps) {
                if (POUtil.isPO(projectCaseSteps)) {
                    try {
                        projectCaseSteps.setStepPath(POUtil.transStepPathToPo(projectCaseSteps));
                    } catch (Exception e) {
                    }
                }
                projectCaseSteps.setStepSerialNumber(stepSerialNumber);
                result = result + projectCaseStepsService.insertProjectCaseSteps(projectCaseSteps);
                stepSerialNumber++;
            }
            return toAjax(result);
        }

        /**
         * 行内子查询步骤
         *
         * @author Seagull
         * @date 2019年5月9日
         */
        @RequiresPermissions("testmanagmt:projectCase:list")
        @RequestMapping(value = "/list")
        public void list (HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setCharacterEncoding("utf-8");
            PrintWriter pw = response.getWriter();
            String caseIdStr = request.getParameter("caseId");
            int caseId = 0;
            // 得到客户端传递的查询参数
            if (StringUtils.isNotEmpty(caseIdStr)) {
                caseId = Integer.parseInt(caseIdStr);
            }

            ProjectCaseSteps projectCaseSteps = new ProjectCaseSteps();
            projectCaseSteps.setCaseId(caseId);
            List<ProjectCaseSteps> stepsList = projectCaseStepsService.selectProjectCaseStepsList(projectCaseSteps);
            for (ProjectCaseSteps pjcs : stepsList) {
                if (POUtil.isPO(pjcs)) {
                    {
                        //页面名称
                        int pageId = Integer.parseInt(pjcs.getStepPath().split("\\.")[0]);
                        //元素名称
                        int elementId = Integer.parseInt(pjcs.getStepPath().split("\\.")[1]);
                        ProjectPageObject tmp = new ProjectPageObject();
                        tmp.setProjectId(pjcs.getProjectId());
                        tmp.setPageId(pageId);
                        ProjectPageObject projectPageObject = projectPageObjectService.selectProjectPageObjectList(tmp).getFirst();
                        String pageName = projectPageObject.getPageName();
                        projectPageObject.setProjectId(pjcs.getProjectId());
                        ProjectPageDetail projectPageDetail = projectPageDetailService.selectProjectPageDetailById(elementId);
                        String element = projectPageDetail.getElement();
                        pjcs.setStepPath(pageName + "." + element);
                    }
                }
            }

            // 转换成json字符串
            JSONArray recordJson = JSONArray.parseArray(JSON.toJSONString(stepsList, SerializerFeature.WriteNullStringAsEmpty));
            pw.print(recordJson);
        }

        /**
         * 修改保存项目测试用例管理
         */
        @RequiresPermissions("testmanagmt:projectCase:edit")
        @Log(title = "测试用例步骤管理", businessType = BusinessType.UPDATE)
        @PostMapping("/stepEditSave")
        @ResponseBody
        public AjaxResult stepEditSave (ProjectCaseSteps projectCaseSteps){
            if (!PermissionUtils.isProjectPermsPassByProjectId(projectCaseSteps.getProjectId())) {
                return error("没有此项目修改用例步骤权限");
            }
            if (POUtil.isPO(projectCaseSteps)) {
                try {
                    projectCaseSteps.setStepPath(POUtil.transStepPathToPo(projectCaseSteps));
                } catch (Exception e) {
                }
            }
            return toAjax(projectCaseStepsService.updateProjectCaseSteps(projectCaseSteps));
        }

        /**
         * @author lifengyang
         * 用例步骤导出
         */
        @RequiresPermissions("testmanagmt:projectCase:exportcasestep")
        @PostMapping("/export")
        @ResponseBody
        public AjaxResult export (@RequestParam("caseId") Integer caseId){
            ProjectCase projectCase = projectCaseService.selectProjectCaseById(caseId);
            ProjectCaseSteps projectCaseSteps = new ProjectCaseSteps();
            projectCaseSteps.setCaseId(caseId);
            List<ProjectCaseSteps> stepsList = projectCaseStepsService.selectProjectCaseStepsList(projectCaseSteps);

            if (stepsList.size() == 0) {
                projectCaseSteps.setAction("");
                projectCaseSteps.setExpectedResult("");
                projectCaseSteps.setExtend("");
                projectCaseSteps.setProjectId(projectCase.getProjectId());
                projectCaseSteps.setStepId(0);
                projectCaseSteps.setStepOperation("");
                projectCaseSteps.setStepParameters("");
                projectCaseSteps.setStepPath("");
                projectCaseSteps.setStepSerialNumber(1);
                projectCaseSteps.setStepType(projectCase.getCaseType());
                stepsList.add(projectCaseSteps);
            }

            ExcelUtil<ProjectCaseSteps> util = new ExcelUtil<>(ProjectCaseSteps.class);
            return util.exportExcel(stepsList, "测试用例步骤");
        }
    }
