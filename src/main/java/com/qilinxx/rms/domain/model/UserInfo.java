package com.qilinxx.rms.domain.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "userinfo")
public class UserInfo {
    @Id
    private String uid;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "sex")
    private String sex;

    @Column(name = "title")
    private String title;

    @Column(name = "belong")
    private String belong;

    @Column(name = "mid")
    private Integer mid;

    @Column(name = "profile")
    private String profile;

    @Column(name = "state")
    private String state;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "update_time")
    private Long updateTime;

    @Column(name = "remake")
    private String remake;

    @Column(name = "birthday")
    private Long birthday;

    /**
     * 学历履历
     */
    @Column(name = "academic_resume")
    private String academicResume;

    /**
     * 职称履历
     */
    @Column(name = "title_resume")
    private String titleResume;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong == null ? null : belong.trim();
    }

    public Integer getMid() {
        return mid;
    }

    public void setMid(Integer mid) {
        this.mid = mid;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile == null ? null : profile.trim();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemake() {
        return remake;
    }

    public void setRemake(String remake) {
        this.remake = remake == null ? null : remake.trim();
    }

    public Long getBirthday() {
        return birthday;
    }

    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }

    public String getAcademicResume() {
        return academicResume;
    }

    public void setAcademicResume(String academicResume) {
        this.academicResume = academicResume;
    }

    public String getTitleResume() {
        return titleResume;
    }

    public void setTitleResume(String titleResume) {
        this.titleResume = titleResume;
    }

//    @Override
//    public String toString() {
//        return "UserInfo{" +
//                "uid=" + uid +
//                ", password='" + password + '\'' +
//                ", name='" + name + '\'' +
//                ", sex='" + sex + '\'' +
//                ", title='" + title + '\'' +
//                ", belong='" + belong + '\'' +
//                ", mid=" + mid +
//                ", profile='" + profile + '\'' +
//                ", state='" + state + '\'' +
//                ", createTime=" + createTime +
//                ", updateTime=" + updateTime +
//                ", remake='" + remake + '\'' +
//                ", birthday='" + birthday + '\'' +
//                '}';
//    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", uid=").append(uid);
        sb.append(", password=").append(password);
        sb.append(", name=").append(name);
        sb.append(", sex=").append(sex);
        sb.append(", title=").append(title);
        sb.append(", belong=").append(belong);
        sb.append(", mid=").append(mid);
        sb.append(", profile=").append(profile);
        sb.append(", state=").append(state);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", remake=").append(remake);
        sb.append(", birthday=").append(birthday);
        sb.append("]");
        return sb.toString();
    }
}