package com.qilinxx.rms.controller;

import com.qilinxx.rms.domain.model.*;
import com.qilinxx.rms.domain.model.vo.UserInfoVo;
import com.qilinxx.rms.service.*;
import com.qilinxx.rms.util.*;
import com.qilinxx.rms.util.UUID;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @Auther: dqsdc
 * @Date: 2019-02-22 16:45
 * @Description:
 */
@Controller
public class AdminController extends BaseController {


    @Autowired
    UserInfoService userInfoService;

    @Autowired
    UserItemService userItemService;

    @Autowired
    UserMajorService userMajorService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ThesisService thesisService;

    @Autowired
    TextbookService textbookService;

    @Autowired
    MeetingService meetingService;

    @Autowired
    DocumentService documentService;

    @Autowired
    MajorService majorService;

    @Autowired
    LogService logService;

    @Autowired
    RewardService rewardService;

    @Autowired
    NoticeService noticeService;

    @Autowired
    ExportExcelService exportExcelService;

    @RequestMapping("/adminIndex")
    public String showIndex(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String i = (String) session.getAttribute("uid");
        UserInfo login = userInfoService.findUserByUid(i);
        if (login.getRemake() == null || !login.getRemake().equals("admin")) {
            session.invalidate();
            return "redirect:/login";
        }
        logService.insertLog("???????????????", "admin", userIp(request));
        return "admin/index";
    }

    @RequestMapping("admin-password")
    public String changeAdminPassword() {
        return "admin/password-change";
    }

    /**
     * @return ????????????????????????
     */
    @GetMapping("admin-textbook-overview")
    public String textbookOverview(HttpSession session, Model model) {
        List<UserItem> userItemList = userItemService.findAllUserItemByUserType("textbook");
        List<Textbook> textbookList = new ArrayList<>();
        Map<String, UserInfo> createrMap = new HashMap<>();
        for (UserItem userItem : userItemList) {
            textbookList.add(textbookService.findTextbookById(userItem.getItemId()));
        }
        for (Textbook textbook : textbookList) {
            createrMap.put(textbook.getCreateId(), userInfoService.findUserByUid(textbook.getCreateId()));
        }
        //???projectList??????
        Collections.reverse(textbookList);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("textbookList", textbookList);
        model.addAttribute("dateKit", new DateKit());
        return "admin/textbook-overview";
    }

    /**
     * @return ????????????????????????
     */
    @GetMapping("admin-meeting-overview")
    public String meetingOverview(HttpSession session, Model model) {
        List<UserItem> userItemList = userItemService.findAllUserItemByUserType("meeting");
        List<Meeting> meetingList = new ArrayList<>();
        Map<String, UserInfo> createrMap = new HashMap<>();
        for (UserItem userItem : userItemList) {
            meetingList.add(meetingService.findMeetingById(userItem.getItemId()));
        }
        for (Meeting meeting : meetingList) {
            createrMap.put(meeting.getCreateId(), userInfoService.findUserByUid(meeting.getCreateId()));
        }
        //???projectList??????
        Collections.reverse(meetingList);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("meetingList", meetingList);
        model.addAttribute("dateKit", new DateKit());
        return "admin/meeting-overview";
    }

    /**
     * @param id       itemId
     * @param itemType item?????????
     * @return ??????item????????????????????????????????????????????????
     */
    @GetMapping("admin-item-detail")
    public String itemDetail(String id, String itemType, Model model, HttpSession session, String from) {
        boolean display = true;
        if (!from.equals("user")) {
            display = false;
        }
        Map<String, UserInfo> createrMap = new HashMap<>();
        switch (itemType) {
            case "project":
                Project project = projectService.findProjectByPid(id);
                if (project.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(project.getCreateId(), userInfoService.findUserByUid(project.getCreateId()));
                model.addAttribute("project", project);
                break;
            case "thesis":
                Thesis thesis = thesisService.findThesisByTid(id);
                if (thesis.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(thesis.getCreateId(), userInfoService.findUserByUid(thesis.getCreateId()));
                model.addAttribute("thesis", thesis);
                break;
            case "reward":
                Reward reward = rewardService.findRewardByRid(id);
                if (reward.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(reward.getCreateId(), userInfoService.findUserByUid(reward.getCreateId()));
                model.addAttribute("reward", reward);
                break;
            case "textbook":
                Textbook textbook = textbookService.findTextbookById(id);
                if (textbook.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(textbook.getCreateId(), userInfoService.findUserByUid(textbook.getCreateId()));
                model.addAttribute("textbook", textbook);
                break;
            case "meeting":
                Meeting meeting = meetingService.findMeetingById(id);
                if (meeting.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(meeting.getCreateId(), userInfoService.findUserByUid(meeting.getCreateId()));
                model.addAttribute("meeting", meeting);
                break;
        }

        List<Document> documentList = documentService.findDocumentByItemId(id);
        model.addAttribute("display", display);
        model.addAttribute("documentList", documentList);
        model.addAttribute("itemType", itemType);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("dateKit", new DateKit());
        return "manager/detail/item-detail";
    }


    /**
     * @param id       itemId
     * @param itemType item?????????
     * @return ??????item???????????????, ???????????????????????????
     */
    @GetMapping("admin-item-detail-file")
    public String itemDetailFile(String id, String itemType, Model model, HttpSession session, String from) {
        boolean display = true;
        if (!from.equals("user")) {
            display = false;
        }
        Map<String, UserInfo> createrMap = new HashMap<>();
        switch (itemType) {
            case "project":
                Project project = projectService.findProjectByPid(id);
                if (project.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(project.getCreateId(), userInfoService.findUserByUid(project.getCreateId()));
                model.addAttribute("project", project);
                break;
            case "thesis":
                Thesis thesis = thesisService.findThesisByTid(id);
                if (thesis.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(thesis.getCreateId(), userInfoService.findUserByUid(thesis.getCreateId()));
                model.addAttribute("thesis", thesis);
                break;
            case "reward":
                Reward reward = rewardService.findRewardByRid(id);
                if (reward.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(reward.getCreateId(), userInfoService.findUserByUid(reward.getCreateId()));
                model.addAttribute("reward", reward);
                break;
            case "textbook":
                Textbook textbook = textbookService.findTextbookById(id);
                if (textbook.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(textbook.getCreateId(), userInfoService.findUserByUid(textbook.getCreateId()));
                model.addAttribute("textbook", textbook);
                break;
            case "meeting":
                Meeting meeting = meetingService.findMeetingById(id);
                if (meeting.getState().equals("2")) {
                    display = false;
                }
                createrMap.put(meeting.getCreateId(), userInfoService.findUserByUid(meeting.getCreateId()));
                model.addAttribute("meeting", meeting);
                break;
        }

        List<Document> documentList = documentService.findDocumentByItemId(id);
        model.addAttribute("display", display);
        model.addAttribute("documentList", documentList);
        model.addAttribute("itemType", itemType);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("dateKit", new DateKit());
        return "manager/detail/item-detail-file";
    }


    @Transactional
    @RequestMapping("/exportUserInfo")
    @ResponseBody
    public void exportUserInfo(String[] chk, HttpServletResponse response) {
//        response.setContentType("application/octet-stream");
//        response.setContentType("application/OCTET-STREAM;charset=UTF-8");
        //1.????????????ContentType?????????????????????????????????????????????????????????
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        System.out.println(Arrays.toString(chk));
        exportExcelService.exportUserInfo(chk, response);
    }

    @Transactional
    @RequestMapping("/exportProject")
    @ResponseBody
    public void exportProject(String[] chk, HttpServletResponse response) {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        System.out.println(Arrays.toString(chk));
        exportExcelService.exportProject(chk, response);
    }

    @Transactional
    @RequestMapping("/exportThesis")
    @ResponseBody
    public void exportThesis(String[] chk, HttpServletResponse response) {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        System.out.println(Arrays.toString(chk));
        exportExcelService.exportThesis(chk, response);
    }

    @Transactional
    @RequestMapping("/exportReward")
    @ResponseBody
    public void exportReward(String[] chk, HttpServletResponse response) {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        System.out.println(Arrays.toString(chk));
        exportExcelService.exportReward(chk, response);
    }

    @Transactional
    @RequestMapping("/exportTextbook")
    @ResponseBody
    public void exportTextbook(String[] chk, HttpServletResponse response) {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        System.out.println(Arrays.toString(chk));
        exportExcelService.exportTextbook(chk, response);
    }

    @Transactional
    @RequestMapping("/exportMeeting")
    @ResponseBody
    public void exportMeeting(String[] chk, HttpServletResponse response) {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".xlsx");
        System.out.println(Arrays.toString(chk));
        exportExcelService.exportMeeting(chk, response);
    }

    @RequestMapping("/welcome")
    public String showWelcome(Model model, HttpSession session) {
        ServletContext servletContext = session.getServletContext();
        Log log = logService.getLastAdminLog();
        Map<String, Object> loginMap = (Map<String, Object>) servletContext.getAttribute("loginMap");
        model.addAttribute("sessionSize", loginMap.size());
        model.addAttribute("log", log);
        model.addAttribute("commons", new Commons());
        return "admin/welcome";
    }

    @RequestMapping("student-list.html")
    public String showMemberList(Model model) {
        List<UserInfoVo> infoList = userInfoService.findAllUser();

        model.addAttribute("dateKit", new DateKit());
        model.addAttribute("userInfoList", infoList);
        model.addAttribute("commons", new Commons());
        return "admin/student-list";
    }

    @RequestMapping("grant-list.html")
    public String showGrantList(Model model) {
        List<UserInfoVo> infoList = userInfoService.findAllUser();
        List<Major> majorList = majorService.findAllMajor();
        model.addAttribute("majorList", majorList);
        model.addAttribute("userInfoList", infoList);
        model.addAttribute("commons", new Commons());
        return "admin/grant-list";
    }

    @RequestMapping(value = "/updatePermission", method = RequestMethod.POST)
    @ResponseBody
    public String updatePermission(@RequestParam("mid") int[] mid, String uid, String power) {
        System.out.println("updatePermission" + mid.length);
        System.out.println("uid:--" + uid);
        System.out.println(power);
        Integer i = userMajorService.updatePermission(uid, mid, power);
        if (i == mid.length)
            return "??????????????????";
        else
            return "????????????";
    }

    @RequestMapping(value = "/cancelPermission", method = RequestMethod.POST)
    @ResponseBody
    public String cancelPermission(String uid, String power) {
        System.out.println("uid:--" + uid);
        Integer i = userMajorService.cancelPermission(uid, power);
        System.out.println(i);
        return "????????????";
    }

    @RequestMapping("/admin-student-add")
    public String Student_add(Model model) {
        List<Major> majorList = majorService.findAllMajor();
        model.addAttribute("majorList", majorList);
        return "admin/student-add";
    }

    /**
     * @Author: LJM
     * @Description: ??????????????????
     */
    @ResponseBody
    @RequestMapping("/addStudent")
    public String addStudent(UserInfo userInfo, HttpServletRequest request) {
        System.out.println(userInfo);
        userInfo.setPassword("111111");
        userInfo.setCreateTime(DateKit.getUnixTimeLong());

        Integer i = userInfoService.insert(userInfo);
        if (i > 0) {
            logService.insertLog("????????????" + userInfo.getName(), "admin", request.getRemoteAddr());
        } else {
            logService.insertLog("??????" + userInfo.getName() + "??????", "admin", request.getRemoteAddr());
        }
        return "????????????";
    }

    /**
     * @Author: LJM
     * @Description: ??????????????????  ????????????????????????
     */
    @ResponseBody
    @RequestMapping("/deleteStudent")
    public String deleteStudent(String uid, HttpServletRequest request) {
        String name = userInfoService.selectNameById(uid);
        Integer i = userInfoService.deleteStudentById(uid);

        if (i > 0) {
            logService.insertLog("????????????????????????" + name, "admin", request.getRemoteAddr());
            return "????????????";
        } else {
            logService.insertLog("??????????????????" + name + "??????", "admin", request.getRemoteAddr());
            return "????????????";
        }

    }

    /**
     * @Author: LJM
     * @Description: ????????????
     */
    @ResponseBody
    @RequestMapping("/stopStudent")
    public String stopStudent(String uid, HttpServletRequest request) {
        Integer i = userInfoService.stopStudent(uid);
        String name = userInfoService.selectNameById(uid);
        if (i > 0) {
            logService.insertLog("????????????" + name, "admin", request.getRemoteAddr());
            return "????????????";
        } else {
            logService.insertLog("??????" + name + "??????", "admin", request.getRemoteAddr());
            return "????????????";
        }
    }

    /**
     * @Author: LJM
     * @Description: ????????????
     */
    @ResponseBody
    @RequestMapping("/startStudent")
    public String startStudent(String uid, HttpServletRequest request) {
        String name = userInfoService.selectNameById(uid);
        Integer i = userInfoService.startStudent(uid);
        logService.insertLog("????????????" + name, "admin", request.getRemoteAddr());
        return "success";
    }

    /**
     * @Author: LJM
     * @Description: ?????????????????????????????????
     */
    @RequestMapping("student-edit.html")
    public String student_edit(String uid, Model model) {
        UserInfo user = userInfoService.findUserByUid(uid);
        List<Major> majors = majorService.findAllMajor();
        List<Document> documentList = documentService.findDocumentByItemId(uid);

        model.addAttribute("birthdayDate", user.getBirthday() == null ? null : DateKit.formatDateByUnixTime(user.getBirthday(), "yyyy-MM"));
        model.addAttribute("majorList", majors);
        model.addAttribute("student", user);
        model.addAttribute("documentList", documentList);
        return "admin/student-edit";
    }

    /**
     * @Author: LJM
     * @Description: ??????????????????
     */
    @ResponseBody
    @RequestMapping("/editStudent")
    public String editStudent(UserInfo user, HttpServletRequest request) {
        userInfoService.editStudent(user);
        logService.insertLog("????????????" + user.getName() + "?????????", "admin", request.getRemoteAddr());
        return "????????????";
    }

    /**
     * @return ????????????????????????
     */
    @GetMapping("admin-project-overview")
    public String projectOverview(Model model) {
        List<UserItem> userItemList = userItemService.findAllUserItemByUserType("project");
        List<Project> projectList = new ArrayList<>();
        Map<String, UserInfo> createrMap = new HashMap<>();
        for (UserItem userItem : userItemList) {
            projectList.add(projectService.findProjectByPid(userItem.getItemId()));
        }
        for (Project project : projectList) {
            createrMap.put(project.getCreateId(), userInfoService.findUserByUid(project.getCreateId()));
        }
        //???projectList??????
        Collections.reverse(projectList);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("projectList", projectList);
        model.addAttribute("dateKit", new DateKit());
        return "admin/project-overview";
    }

    //????????????????????????
    @GetMapping("admin-thesis-overview")
    public String thesisOverview(Model model) {
        List<UserItem> userItemList = userItemService.findAllUserItemByUserType("thesis");
        List<Thesis> thesisList = new ArrayList<>();
        Map<String, UserInfo> createrMap = new HashMap<>();
        for (UserItem userItem : userItemList) {
            thesisList.add(thesisService.findThesisByTid(userItem.getItemId()));
        }
        for (Thesis thesis : thesisList) {
            createrMap.put(thesis.getCreateId(), userInfoService.findUserByUid(thesis.getCreateId()));
        }
        //???projectList??????
        Collections.reverse(thesisList);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("thesisList", thesisList);
        model.addAttribute("dateKit", new DateKit());
        return "admin/thesis-overview";
    }

    // ????????????????????????
    @GetMapping("admin-reward-overview")
    public String rewardOverview(Model model) {
        List<UserItem> userItemList = userItemService.findAllUserItemByUserType("reward");
        List<Reward> rewardList = new ArrayList<>();
        Map<String, UserInfo> createrMap = new HashMap<>();
        for (UserItem userItem : userItemList) {
            rewardList.add(rewardService.findRewardByRid(userItem.getItemId()));
        }
        for (Reward reward : rewardList) {
            createrMap.put(reward.getCreateId(), userInfoService.findUserByUid(reward.getCreateId()));
        }
        //???rewardList??????
        Collections.reverse(rewardList);
        model.addAttribute("createrMap", createrMap);
        model.addAttribute("rewardList", rewardList);
        model.addAttribute("dateKit", new DateKit());
        return "admin/reward-overview";
    }

    /**
     * @param id
     * @return ????????????????????????
     */
    @GetMapping("admin-meeting-edit")
    public String meetingEdit(String id, Model model) {
        Meeting meeting = meetingService.findMeetingById(id);
        String uid = meeting.getCreateId();
        List<Document> documentList = documentService.findDocumentByItemId(id);
        FileKit.meetingMap = FileKit.clearOrInitMap(FileKit.meetingMap, uid);
        FileKit.deleteFile(new File(UploadUtil.getUploadFilePath() + "/upload//admin//temp//meeting"));
        model.addAttribute("documentList", documentList);
        model.addAttribute("meeting", meeting);
        model.addAttribute("startTime",DateKit.formatDateByUnixTime(meeting.getStartTime(),"yyyy-MM-dd HH:mm"));
        model.addAttribute("endTime",DateKit.formatDateByUnixTime(meeting.getEndTime(),"yyyy-MM-dd HH:mm"));
        model.addAttribute("key", java.util.UUID.randomUUID().toString().replace("-", "") + "-" + uid);
        return "admin/edit/meeting-edit";
    }

    /**
     * ajax ?????????????????????
     *
     * @param file ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-meeting-file")
    @ResponseBody
    public JSONObject ajaxMeetingFile(MultipartFile file, HttpSession session, String key) throws IOException {
        JSONObject json = new JSONObject();
        UserInfo user = userInfoService.findUserByUid((String) session.getAttribute("uid"));

        List<MultipartFile> meetingFileList = FileKit.meetingMap.get(key);
        if (meetingFileList == null) meetingFileList = FileKit.clearOrInitList(meetingFileList);
        //????????????
        int i = 0;
        if (meetingFileList.size() != 0) {
            for (MultipartFile multipartFile : meetingFileList) {
                if (multipartFile.getOriginalFilename().equals(file.getOriginalFilename())) {
                    i++;
                }
            }
        }
        if (i == 0) {
            meetingFileList.add(file);
            FileKit.meetingMap.put(key, meetingFileList);

            String path = UploadUtil.getUploadFilePath() + "/upload//admin//temp//meeting";//?????????????????? ??????????????????
            File dirFile = new File(path);
            dirFile.mkdirs();
            String fileName = file.getOriginalFilename();//????????????
            File targetFile = new File(path, fileName);
            FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(targetFile));
        }
        return json;
    }

    /**
     * ajax????????????????????????????????????
     */
    @PostMapping("admin-meeting-edit-form")
    @ResponseBody
    public JSONObject ajaxMeetingEditForm(String key, Meeting meeting, String startTimeDate, String endTimeDate) throws IOException {
        JSONObject json = new JSONObject();
        //?????????????????????
        startTimeDate += ":00";
        endTimeDate += ":00";
        meeting.setStartTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(startTimeDate)))));
        meeting.setEndTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(endTimeDate)))));
        Integer meetingNum = meetingService.countMeetingByNameMeetingTimeExceptId(meeting.getName(), meeting.getStartTime(), meeting.getId());
        if (meetingNum != 0) {
            json.put("msg", "????????????????????????");
            return json;
        }
        String createId = meeting.getCreateId();//?????????id
        UserInfo user = userInfoService.findUserByUid(createId);
        System.out.println(user);
        //??????????????????????????????
        String member= meeting.getPeople().replace("???", ",").replace("???", ",").trim();
        String[] names =member.split(",");
        Map<String, String> nameMap = new HashMap<>();
        String people = "";
        for (String name : names) {
            if (!name.equals("")) {
                nameMap.put(name, "");
            }
        }
        Iterator iterator1 = nameMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator1.next();
            people = people + (String) entry.getKey() + ",";
        }
        if (!nameMap.containsKey(user.getName())) {
            json.put("msg", "????????????????????????????????????");
            return json;
        }
        meeting.setPeople(member);
        /**
         * ??????????????????
         */

        //??????????????????
        //meeting.setState("0");
        meeting.setState("2");
        meeting.setUpdateTime(DateKit.getUnixTimeLong());
        meetingService.updateMeeting(meeting);
        /**
         * ?????????????????????????????????????????????
         */
        //???????????????
        userItemService.deleteUserItemByItemId(meeting.getId());
        //???????????????
        UserItem userItem = new UserItem();
        userItem.setItemId(meeting.getId());
        userItem.setItemType("meeting");
        Iterator iterator2 = nameMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator2.next();
            List<UserInfo> userInfoList = userInfoService.findUserByName((String) entry.getKey());
            if (userInfoList.size() != 0) {
                userItem.setUid(userInfoList.get(0).getUid());
                userItemService.createUserItem(userItem);
            }
        }
        List<MultipartFile> meetingFileList = FileKit.meetingMap.get(key);
        if (meetingFileList != null && meetingFileList.size() != 0) {
            Meeting dbMeeting = meetingService.findMeetingById(meeting.getId());
            Document document = new Document();
            document.setItemId(meeting.getId());
            document.setItemType("meeting");
            String fileName = "";
            for (MultipartFile file : meetingFileList) {
                int i = 0;
                fileName = file.getOriginalFilename();
                List<Document> documentList = documentService.findDocumentByItemId(meeting.getId());
                for (Document d : documentList) {
                    if (d.getName().equals(fileName)) i++;
                }
                if (i != 0) {
                    continue;
                }
                document.setName(fileName);
                document.setType(fileName.substring(fileName.lastIndexOf(".")));
                document.setPath("upload"+File.separator+"admin"+File.separator+"meeting"+File.separator + dbMeeting.getCreateTime() + File.separator + fileName);
                documentService.createDocument(document);
                //?????????????????????
                File tempFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"meeting", fileName);
                File targetFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"meeting"+File.separator + dbMeeting.getCreateTime(), fileName);
                File pathFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"meeting"+File.separator + dbMeeting.getCreateTime());
                pathFile.mkdirs();
                FileCopyUtils.copy(tempFile, targetFile);
                FileKit.deleteFile(tempFile);
            }
            FileKit.meetingMap.remove(key);
            File dirFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"meeting");
            FileKit.deleteFile(dirFile);
        }
        json.put("msg", "????????????????????????");
        return json;
    }

    /**
     * @param id
     * @return ????????????????????????
     */
    @GetMapping("admin-project-edit")
    public String projectEdit(String id, Model model) {
        Project project = projectService.findProjectByPid(id);
        String uid = project.getCreateId();
        List<Document> documentList = documentService.findDocumentByItemId(id);
        FileKit.projectMap = FileKit.clearOrInitMap(FileKit.projectMap, uid);
        FileKit.deleteFile(new File(UploadUtil.getUploadFilePath() + File.separator+ "upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"project"));
        model.addAttribute("documentList", documentList);
        model.addAttribute("project", project);
        model.addAttribute("startTime",DateKit.formatDateByUnixTime(project.getStartTime(),"yyyy-MM-dd"));
        model.addAttribute("endTime",DateKit.formatDateByUnixTime(project.getEndTime(),"yyyy-MM-dd"));
        model.addAttribute("setTime",DateKit.formatDateByUnixTime(project.getSetTime(),"yyyy-MM-dd"));
        model.addAttribute("key", java.util.UUID.randomUUID().toString().replace("-", "") + "-" + uid);
        return "admin/edit/project-edit";
    }

    /**
     * ajax ??????????????????
     *
     * @param file ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-project-file")
    @ResponseBody
    public JSONObject ajaxProjectFile(String key,MultipartFile file, HttpSession session) throws IOException {
        JSONObject json = new JSONObject();
        UserInfo user = userInfoService.findUserByUid((String) session.getAttribute("uid"));

        List<MultipartFile> projectFileList = FileKit.projectMap.get(key);
        if (projectFileList==null) projectFileList=FileKit.clearOrInitList(projectFileList);
        //????????????
        int i = 0;
        if (projectFileList.size() != 0) {
            for (MultipartFile multipartFile : projectFileList) {
                if (multipartFile.getOriginalFilename().equals(file.getOriginalFilename())) {
                    i++;
                }
            }
        }
        if (i == 0) {
            projectFileList.add(file);
            FileKit.projectMap.put(key,projectFileList);

            String path = UploadUtil.getUploadFilePath() + File.separator + "upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"project";//?????????????????? ??????????????????
            File dirFile = new File(path);
            dirFile.mkdirs();
            String fileName = file.getOriginalFilename();//????????????
            File targetFile = new File(path, fileName);
            FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(targetFile));
        }
        return json;
    }
    /**
     *  ajax????????????????????????????????????
     * @param id    ??????id
     * @param startTimeDate ????????????
     * @param endTimeDate   ????????????
     * @param setTimeDate   ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-project-edit-form")
    @ResponseBody
    public JSONObject ajaxProjectEditForm(String key,String id,HttpSession session, Project project, String startTimeDate, String endTimeDate, String setTimeDate) throws IOException {
        JSONObject json = new JSONObject();
        project.setPid(id);
        //?????????????????????
        int projectNum = projectService.countProjectByNameHostFromExceptPid(project.getName(), project.getHost(), project.getProjectSource(),project.getPid());
        if (projectNum != 0) {
            json.put("msg", "????????????????????????");
            return json;
        }
        if(projectService.countProjectByTopicExceptPid(project.getTopic(),project.getPid())!=0){
            json.put("msg", "??????????????????????????????");
            return json;
        }
        String cid=project.getCreateId();
        UserInfo user = userInfoService.findUserByUid(cid);
        //??????????????????????????????
        String member=project.getPeople().replace("???", ",").replace("???", ",").trim();
        String[] names = member.split(",");
        Map<String, String> nameMap = new HashMap<>();
        String people = "";
        for (String name : names) {
            if (!name.equals("")) {
                nameMap.put(name, "");
            }
        }
        Iterator iterator1 = nameMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator1.next();
            people = people + (String) entry.getKey() + ",";
        }
        nameMap.put(project.getHost(), "");//???????????????
        if (!nameMap.containsKey(user.getName())) {
            json.put("msg", "????????????????????????????????????");
            return json;
        }
        project.setPeople(member);
        /**
         * ??????????????????
         */

        //???????????????????????????
        startTimeDate += " 00:00:00";
        endTimeDate += " 00:00:00";
        setTimeDate += " 00:00:00";
        project.setStartTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(startTimeDate)))));
        project.setEndTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(endTimeDate)))));
        project.setSetTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(setTimeDate)))));
        //project.setState("0");
        project.setState("2");
        project.setUpdateTime(DateKit.getUnixTimeLong());
        projectService.updateProject(project);
        /**
         * ????????????????????????????????????
         */
        //??????????????????????????????????????????
        userItemService.deleteUserItemByItemId(project.getPid());
        //???????????????????????????????????????
        UserItem userItem = new UserItem();
        userItem.setItemId(project.getPid());
        userItem.setItemType("project");
        Iterator iterator2 = nameMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator2.next();
            List<UserInfo> userInfoList = userInfoService.findUserByName((String) entry.getKey());
            if (userInfoList.size() != 0) {
                userItem.setUid(userInfoList.get(0).getUid());
                userItemService.createUserItem(userItem);
            }
        }
        List<MultipartFile> projectFileList = FileKit.projectMap.get(key);
        if(projectFileList!=null && projectFileList.size()!=0){
            Project dbProject = projectService.findProjectByPid(id);
            Document document = new Document();
            document.setItemId(project.getPid());
            document.setItemType("project");
            String fileName = "";

            for (MultipartFile file : projectFileList) {
                int i=0;
                fileName = file.getOriginalFilename();
                List<Document> documentList = documentService.findDocumentByItemId(project.getPid());
                for (Document d:documentList) {
                    if(d.getName().equals(fileName)) i++;
                }
                if(i!=0){
                    continue;
                }
                document.setName(fileName);
                document.setType(fileName.substring(fileName.lastIndexOf(".")));
                document.setPath("upload"+File.separator+"admin"+File.separator+"project"+File.separator + dbProject.getCreateTime() + File.separator + fileName);
                documentService.createDocument(document);
                //?????????????????????
                File tempFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"project", fileName);
                File targetFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"project"+File.separator + dbProject.getCreateTime(), fileName);
                File pathFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"project"+File.separator + dbProject.getCreateTime());
                pathFile.mkdirs();
                FileCopyUtils.copy(tempFile, targetFile);
                FileKit.deleteFile(tempFile);
            }
            FileKit.projectMap.remove(key);
            File dirFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"project");
            FileKit.deleteFile(dirFile);
        }
        json.put("msg", "????????????????????????");
        return json;
    }

    /**
     * @param id
     * @return ????????????????????????
     */
    @GetMapping("admin-thesis-edit")
    public String thesisEdit(String id, Model model) {

        Thesis thesis = thesisService.findThesisByTid(id);
        String uid = thesis.getCreateId();
        List<Document> documentList = documentService.findDocumentByItemId(id);
        FileKit.thesisMap = FileKit.clearOrInitMap(FileKit.thesisMap, uid);
        FileKit.deleteFile(new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"thesis"));
        model.addAttribute("documentList", documentList);
        model.addAttribute("thesis", thesis);
        model.addAttribute("publishTime",DateKit.formatDateByUnixTime(thesis.getPublishTime(),"yyyy-MM-dd"));
        model.addAttribute("key", java.util.UUID.randomUUID().toString().replace("-", "") + "-" + uid);
        return "admin/edit/thesis-edit";
    }
    /**
     * ajax ??????????????????
     *
     * @param file ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-thesis-file")
    @ResponseBody
    public JSONObject ajaxThesisFile(String key,MultipartFile file, HttpSession session) throws IOException {
        JSONObject json = new JSONObject();
        UserInfo user = userInfoService.findUserByUid((String) session.getAttribute("uid"));
        List<MultipartFile> thesisFileList = FileKit.thesisMap.get(key);
        if (thesisFileList==null) thesisFileList=FileKit.clearOrInitList(thesisFileList);
        //????????????
        int i = 0;
        if (thesisFileList.size() != 0) {
            for (MultipartFile multipartFile : thesisFileList) {
                if (multipartFile.getOriginalFilename().equals(file.getOriginalFilename())) {
                    i++;
                }
            }
        }
        if (i == 0) {
            thesisFileList.add(file);
            FileKit.thesisMap.put(key,thesisFileList);

            String path = UploadUtil.getUploadFilePath() + File.separator+ "upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"thesis";//?????????????????? ??????????????????
            File dirFile = new File(path);
            dirFile.mkdirs();
            String fileName = file.getOriginalFilename();//????????????
            File targetFile = new File(path, fileName);
            FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(targetFile));
        }
        return json;
    }
    /**
     * ajax????????????????????????????????????
     *
     * @param startPage ????????????
     * @param endPage   ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-thesis-edit-form")
    @ResponseBody
    public JSONObject ajaxThesisEditForm(String publishTimeDate,String key,String id,Thesis thesis, Integer startPage, Integer endPage, HttpSession session) throws IOException {
        JSONObject json = new JSONObject();
        thesis.setTid(id);
        //??????????????????
        if(thesis.getDossier()==null&&thesis.getIssue()==null){
            json.put("msg", "????????????????????????");
            return json;
        }
        int thesisNum = thesisService.countThesisByHostNameExceptTid(thesis.getHost(), thesis.getName(),thesis.getTid());
        if (thesisNum != 0) {
            json.put("msg", "????????????????????????");
            return json;
        }
        String cid=thesis.getCreateId();
        UserInfo user = userInfoService.findUserByUid(cid);
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(thesis.getHost(), "");//???????????????
        //??????????????????????????????
        if (thesis.getPeople()!=null&&!"".equals(thesis.getPeople())) {
            String member=thesis.getPeople().replace("???", ",").replace("???", ",").trim();
            String[] names = member.split(",");
            String people = "";
            for (String name : names) {
                if (!name.equals("")) {
                    nameMap.put(name, "");
                }
            }
            Iterator iterator1 = nameMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator1.next();
                people = people + (String) entry.getKey() + ",";
            }
            nameMap.put(thesis.getHost(), "");//???????????????
            thesis.setPeople(member);
        }
        if (!nameMap.containsKey(user.getName())) {
            json.put("msg", "????????????????????????????????????");
            return json;
        }
        /**
         * ??????????????????
         */
        publishTimeDate += " 00:00:00";
        thesis.setPublishTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(publishTimeDate)))));
        thesis.setPageNum(startPage + "-" + endPage);
        //thesis.setState("0");
        thesis.setState("2");
        thesis.setUpdateTime(DateKit.getUnixTimeLong());
        thesisService.updateThesis(thesis);
        if (thesis.getDossier()==null&& thesis.getIssue()!=null)
            thesisService.setDossierNull(thesis.getTid());
        if (thesis.getDossier()!=null&& thesis.getIssue()==null)
            thesisService.setIssueNull(thesis.getTid());
        /**
         * ?????????????????????????????????????????????
         */
        //???????????????????????????
        userItemService.deleteUserItemByItemId(thesis.getTid());
        //????????????????????????
        UserItem userItem = new UserItem();
        userItem.setItemId(thesis.getTid());
        userItem.setItemType("thesis");
        Iterator iterator2 = nameMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator2.next();
            List<UserInfo> userInfoList = userInfoService.findUserByName((String) entry.getKey());
            if (userInfoList.size() != 0) {
                userItem.setUid(userInfoList.get(0).getUid());
                userItemService.createUserItem(userItem);
            }
        }
        List<MultipartFile> thesisFileList=FileKit.thesisMap.get(key);
        if(thesisFileList!=null && thesisFileList.size()!=0) {
            Thesis dbThesis = thesisService.findThesisByTid(id);
            Document document = new Document();
            document.setItemId(thesis.getTid());
            document.setItemType("thesis");
            String fileName = "";
            for (MultipartFile file : thesisFileList) {
                int i=0;
                fileName = file.getOriginalFilename();
                List<Document> documentList = documentService.findDocumentByItemId(thesis.getTid());
                for (Document d:documentList) {
                    if(d.getName().equals(fileName)) i++;
                }
                if(i!=0){
                    continue;
                }
                document.setName(fileName);
                document.setType(fileName.substring(fileName.lastIndexOf(".")));
                document.setPath("upload"+File.separator+"admin"+File.separator+"thesis" + File.separator + dbThesis.getCreateTime() + File.separator + fileName);
                documentService.createDocument(document);
                //?????????????????????
                File tempFile = new File(UploadUtil.getUploadFilePath() + File.separator + "upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"thesis", fileName);
                File targetFile = new File(UploadUtil.getUploadFilePath() + File.separator +"upload"+File.separator+"admin"+File.separator+"thesis" + File.separator + dbThesis.getCreateTime(), fileName);
                File pathFile = new File(UploadUtil.getUploadFilePath() + File.separator + "upload"+File.separator+"admin"+File.separator+"thesis"+File.separator + dbThesis.getCreateTime());
                pathFile.mkdirs();
                FileCopyUtils.copy(tempFile, targetFile);
                FileKit.deleteFile(tempFile);
            }
            FileKit.thesisMap.remove(key);
            File dirFile = new File(UploadUtil.getUploadFilePath() + File.separator+ "upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"thesis");
            FileKit.deleteFile(dirFile);
        }
        json.put("msg", "????????????????????????");
        return json;
    }


    /**
     * @param id
     * @return ????????????????????????
     */
    @GetMapping("admin-reward-edit")
    public String rewardEdit(String id, Model model) {
        Reward reward = rewardService.findRewardByRid(id);
        String uid = reward.getCreateId();
        List<Document> documentList = documentService.findDocumentByItemId(id);
        FileKit.rewardMap = FileKit.clearOrInitMap(FileKit.rewardMap, uid);
        FileKit.deleteFile(new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"reward"));
        model.addAttribute("documentList", documentList);
        model.addAttribute("reward", reward);
        model.addAttribute("getTime",DateKit.formatDateByUnixTime(reward.getGetTime(),"yyyy-MM-dd"));
        model.addAttribute("key", java.util.UUID.randomUUID().toString().replace("-", "") + "-" + uid);
        return "admin/edit/reward-edit";
    }
    /**
     * ajax ??????????????????
     *
     * @param file ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-reward-file")
    @ResponseBody
    public JSONObject ajaxRewardFile(String key,MultipartFile file, HttpSession session) throws IOException {
        JSONObject json = new JSONObject();
        List<MultipartFile> rewardFileList = FileKit.rewardMap.get(key);
        if (rewardFileList==null) rewardFileList=FileKit.clearOrInitList(rewardFileList);
        //????????????
        int i = 0;
        if (rewardFileList.size() != 0) {
            for (MultipartFile multipartFile : rewardFileList) {
                if (multipartFile.getOriginalFilename().equals(file.getOriginalFilename())) {
                    i++;
                }
            }
        }
        if (i == 0) {
            rewardFileList.add(file);
            FileKit.rewardMap.put(key,rewardFileList);

            String path = UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"reward";//?????????????????? ??????????????????
            File dirFile = new File(path);
            dirFile.mkdirs();
            String fileName = file.getOriginalFilename();//????????????
            File targetFile = new File(path, fileName);
            FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(targetFile));
        }
        return json;
    }
    /**
     * ajax??????????????????????????????
     */
    @PostMapping("admin-ajax-reward-edit-form")
    @ResponseBody
    public JSONObject ajaxRewardEditForm(String key,String id, Reward reward, String getTimeDate) throws IOException {
        JSONObject json = new JSONObject();
        reward.setRid(id);
        //????????????????????????
        getTimeDate += " 00:00:00";
        reward.setGetTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(getTimeDate)))));
        String cid=reward.getCreateId();
        UserInfo user = userInfoService.findUserByUid(cid);
        //??????????????????????????????
        String member= reward.getPeople().replace("???", ",").replace("???", ",").trim();
        String[] names =member.split(",");
        Map<String, String> nameMap = new HashMap<>();
        String people = "";
        for (String name : names) {
            if (!name.equals("")) {
                nameMap.put(name, "");
            }
        }
        Iterator iterator1 = nameMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator1.next();
            people = people + (String) entry.getKey() + ",";
        }
        reward.setPeople(member);

        //??????????????????
        int rewardNum = rewardService.countRewardByNamePeopleGetTimeExceptRid(reward.getName(), reward.getPeople(), reward.getGetTime(),reward.getRid());
        if (rewardNum != 0) {
            json.put("msg", "????????????????????????");
            return json;
        }
        if (!nameMap.containsKey(user.getName())) {
            json.put("msg", "????????????????????????????????????");
            return json;
        }
        /**
         * ??????????????????
         */
        //???????????????????????????
        reward.setGetTime(Long.parseLong(String.valueOf(DateKit.getUnixTimeByDate(DateKit.dateFormat(getTimeDate)))));
        //reward.setState("0");
        reward.setState("2");
        reward.setUpdateTime(DateKit.getUnixTimeLong());
        rewardService.updateReward(reward);
        /**
         * ????????????????????????????????????
         */
        //????????????
        userItemService.deleteUserItemByItemId(reward.getRid());
        //????????????
        UserItem userItem = new UserItem();
        userItem.setItemId(reward.getRid());
        userItem.setItemType("reward");
        Iterator iterator2 = nameMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator2.next();
            List<UserInfo> userInfoList = userInfoService.findUserByName((String) entry.getKey());
            if (userInfoList.size() != 0) {
                userItem.setUid(userInfoList.get(0).getUid());
                userItemService.createUserItem(userItem);
            }
        }
        List<MultipartFile> rewardFileList=FileKit.rewardMap.get(key);
        if(rewardFileList!=null && rewardFileList.size()!=0) {
            Reward dbReward = rewardService.findRewardByRid(id);
            Document document = new Document();
            document.setItemId(reward.getRid());
            document.setItemType("reward");
            String fileName = "";
            for (MultipartFile file : rewardFileList) {
                int i=0;
                fileName = file.getOriginalFilename();
                List<Document> documentList = documentService.findDocumentByItemId(reward.getRid());
                for (Document d:documentList) {
                    if(d.getName().equals(fileName)) i++;
                }
                if(i!=0){
                    continue;
                }
                document.setName(fileName);
                document.setType(fileName.substring(fileName.lastIndexOf(".")));
                document.setPath("upload"+File.separator+"admin"+File.separator+"reward"+File.separator + dbReward.getCreateTime() + File.separator + fileName);
                documentService.createDocument(document);
                //?????????????????????
                File tempFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"reward", fileName);
                File targetFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"reward"+File.separator + dbReward.getCreateTime(), fileName);
                File pathFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"reward"+File.separator + dbReward.getCreateTime());
                pathFile.mkdirs();
                FileCopyUtils.copy(tempFile, targetFile);
                FileKit.deleteFile(tempFile);
            }
            FileKit.rewardMap.remove(key);
            File dirFile = new File(UploadUtil.getUploadFilePath() + File.separator + "upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"reward");
            FileKit.deleteFile(dirFile);
        }
        json.put("msg", "????????????????????????");
        return json;
    }

    /**
     * @param id
     * @return ????????????????????????
     */
    @GetMapping("admin-textbook-edit")
    public String textbookEdit(String id, Model model) {
        Textbook textbook = textbookService.findTextbookById(id);
        String uid = textbook.getCreateId();
        List<Document> documentList = documentService.findDocumentByItemId(id);
        FileKit.textbookMap = FileKit.clearOrInitMap(FileKit.textbookMap, uid);
        FileKit.deleteFile(new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"textbook"));
        List<Category> categories = Arrays.asList(Category.values());
        model.addAttribute("categories", categories);
        model.addAttribute("documentList", documentList);
        model.addAttribute("textbook", textbook);
        model.addAttribute("publishTime",DateKit.formatDateByUnixTime(textbook.getPublishTime(),"yyyy-MM"));
        model.addAttribute("key", java.util.UUID.randomUUID().toString().replace("-", "") + "-" + uid);
        return "admin/edit/textbook-edit";
    }

    /**
     * ajax ?????????????????????
     *
     * @param file ????????????
     * @throws IOException
     */
    @PostMapping("admin-ajax-textbook-file")
    @ResponseBody
    public JSONObject ajaxTextbookFile(String key, MultipartFile file, HttpSession session) throws IOException {
        JSONObject json = new JSONObject();
        UserInfo user = userInfoService.findUserByUid((String) session.getAttribute("uid"));
        List<MultipartFile> textbookFileList = FileKit.textbookMap.get(key);
        if (textbookFileList == null) textbookFileList = FileKit.clearOrInitList(textbookFileList);
        //????????????
        int i = 0;
        if (textbookFileList.size() != 0) {
            for (MultipartFile multipartFile : textbookFileList) {
                if (multipartFile.getOriginalFilename().equals(file.getOriginalFilename())) {
                    i++;
                }
            }
        }
        if (i == 0) {
            textbookFileList.add(file);
            FileKit.textbookMap.put(key, textbookFileList);

            String path = UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"textbook";//?????????????????? ??????????????????
            File dirFile = new File(path);
            dirFile.mkdirs();
            String fileName = file.getOriginalFilename();//????????????
            File targetFile = new File(path, fileName);
            FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(targetFile));
        }
        return json;
    }
    /**
     * ajax??????????????????????????????
     */
    @PostMapping("admin-ajax-textbook-edit-form")
    @ResponseBody
    public JSONObject ajaxTextbookEditForm(String key,Textbook textbook, String publishTimeDate) throws IOException {
        JSONObject json = new JSONObject();
        //??????2?????????
        Integer textBookNum = textbookService.countTextBookByISBNExceptId(textbook.getIsbn(),textbook.getId());
        if (textBookNum != 0) {
            json.put("msg", "?????????/?????????????????????");
            return json;
        }

        String cid=textbook.getCreateId();
        UserInfo user = userInfoService.findUserByUid(cid);
        //??????????????????????????????
        String member=textbook.getPeople().replace("???", ",").replace("???", ",").trim();
        String[] names = member.split(",");
        Map<String, String> nameMap = new HashMap<>();
        String people = "";
        for (String name : names) {
            if (!name.equals("")) {
                nameMap.put(name, "");
            }
        }
        Iterator iterator1 = nameMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator1.next();
            people = people + (String) entry.getKey() + ",";
        }
        if (!nameMap.containsKey(user.getName())) {
            json.put("msg", "?????????/?????????????????????????????????");
            return json;
        }
        textbook.setPeople(member);
        /**
         * ??????????????????
         */
        //???????????????????????????
        publishTimeDate += "-01 00:00:00";
        Date date=DateKit.dateFormat(publishTimeDate);
        Calendar n = Calendar.getInstance( );
        n.setTime(date);
        int month=n.get(Calendar.MONTH);
        n.set(Calendar.MONTH,month);
        textbook.setPublishTime(DateKit.getUnixTimeLong(n.getTime()));
        //textbook.setState("0");
        textbook.setState("2");
        textbook.setUpdateTime(DateKit.getUnixTimeLong());
        textbookService.updateTextbook(textbook);
        /**
         * ????????????????????????????????????
         */
        //???????????????
        userItemService.deleteUserItemByItemId(textbook.getId());
        //???????????????
        UserItem userItem = new UserItem();
        userItem.setItemId(textbook.getId());
        userItem.setItemType("textbook");
        Iterator iterator2 = nameMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator2.next();
            List<UserInfo> userInfoList = userInfoService.findUserByName((String) entry.getKey());
            if (userInfoList.size() != 0) {
                userItem.setUid(userInfoList.get(0).getUid());
                userItemService.createUserItem(userItem);
            }
        }
        List<MultipartFile> textbookFileList=FileKit.textbookMap.get(key);
        if(textbookFileList!=null && textbookFileList.size()!=0) {
            Textbook dbTextbook = textbookService.findTextbookById(textbook.getId());
            Document document = new Document();
            document.setItemId(textbook.getId());
            document.setItemType("textbook");
            String fileName = "";
            for (MultipartFile file : textbookFileList) {
                int i=0;
                fileName = file.getOriginalFilename();
                List<Document> documentList = documentService.findDocumentByItemId(textbook.getId());
                for (Document d:documentList) {
                    if(d.getName().equals(fileName)) i++;
                }
                if(i!=0){
                    continue;
                }
                document.setName(fileName);
                document.setType(fileName.substring(fileName.lastIndexOf(".")));
                document.setPath("upload"+File.separator+"admin"+File.separator+"textbook"+File.separator + dbTextbook.getCreateTime() + File.separator + fileName);
                documentService.createDocument(document);
                //?????????????????????
                File tempFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"textbook", fileName);
                File targetFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"textbook"+File.separator + dbTextbook.getCreateTime(), fileName);
                File pathFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"textbook"+File.separator + dbTextbook.getCreateTime());
                pathFile.mkdirs();
                FileCopyUtils.copy(tempFile, targetFile);
                FileKit.deleteFile(tempFile);
            }
            FileKit.textbookMap.remove(key);
            File dirFile = new File(UploadUtil.getUploadFilePath() + File.separator+"upload"+File.separator+"admin"+File.separator+"temp"+File.separator+"textbook");
            FileKit.deleteFile(dirFile);
        }
        json.put("msg", "????????????????????????");
        return json;
    }
}

