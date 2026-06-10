package com.example.learningplatform.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learningplatform.dto.LQAIDetermineResponse;
import com.example.learningplatform.dto.LearningProfileDTO;
import com.example.learningplatform.dto.SubmitAssessmentRequest;
import com.example.learningplatform.entity.LearningProfile;
import com.example.learningplatform.repository.LearningProfileRepository;

@Service
public class LearningProfileService {

    @Autowired
    private LearningProfileRepository learningProfileRepository;

    @Transactional
    public LQAIDetermineResponse submitAssessment(SubmitAssessmentRequest request) {
        Long userId = request.getUserId();
        Long subjectId = request.getSubjectId();

        Optional<LearningProfile> profileOpt = learningProfileRepository.findByUserIdAndSubjectId(userId, subjectId);
        
        LearningProfile profile;
        if (profileOpt.isEmpty()) {
            profile = new LearningProfile();
            profile.setUserId(userId);
            profile.setSubjectId(subjectId);
            profile.setCreatedAt(LocalDateTime.now());
        } else {
            profile = profileOpt.get();
        }

        profile.setCurrentLevel(convertAnswerToScore(request.getQ1()));
        profile.setLearningSpeed(convertAnswerToScore(request.getQ2()));
        profile.setPreference(convertPreference(request.getQ3()));
        profile.setSelfDiscipline(convertAnswerToScore(request.getQ4()));
        profile.setUpdatedAt(LocalDateTime.now());

        String lqaiCode = determineLQAI_Code(
            profile.getCurrentLevel(),
            profile.getLearningSpeed(),
            profile.getPreference(),
            profile.getSelfDiscipline()
        );
        
        String lqai = getLQAIName(lqaiCode);
        String profileDesc = getLQAIProfile(lqaiCode);

        profile.setLQAI(lqai);
        profile.setLQAI_code(lqaiCode);
        profile.setProfile(profileDesc);

        learningProfileRepository.save(profile);

        return new LQAIDetermineResponse(lqai, lqaiCode, profileDesc);
    }

    private Integer convertAnswerToScore(String answer) {
        if (answer == null) {
            return 3;
        }
        return switch (answer.toUpperCase()) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            default -> 3;
        };
    }

    private String convertPreference(String answer) {
        if (answer == null) {
            return "视频";
        }
        return switch (answer.toUpperCase()) {
            case "A" -> "视频";
            case "B" -> "文本";
            case "C" -> "音频";
            case "D" -> "实践";
            default -> "视频";
        };
    }

    @Transactional
    public LQAIDetermineResponse determineLQAI(Long userId, Long subjectId) {
        Optional<LearningProfile> profileOpt = learningProfileRepository.findByUserIdAndSubjectId(userId, subjectId);
        
        if (profileOpt.isEmpty()) {
            throw new RuntimeException("学习档案不存在");
        }

        LearningProfile profile = profileOpt.get();
        
        String lqaiCode = determineLQAI_Code(
            profile.getCurrentLevel(),
            profile.getLearningSpeed(),
            profile.getPreference(),
            profile.getSelfDiscipline()
        );
        
        String lqai = getLQAIName(lqaiCode);
        String profileDesc = getLQAIProfile(lqaiCode);

        profile.setLQAI(lqai);
        profile.setLQAI_code(lqaiCode);
        profile.setProfile(profileDesc);
        profile.setUpdatedAt(LocalDateTime.now());
        
        learningProfileRepository.save(profile);

        return new LQAIDetermineResponse(lqai, lqaiCode, profileDesc);
    }

    private String determineLQAI_Code(Integer currentLevel, Integer learningSpeed, 
                                      String preference, Integer selfDiscipline) {
        if (currentLevel == null || learningSpeed == null || preference == null || selfDiscipline == null) {
            return "UNKNOWN";
        }

        String levelCode = getLevelCode(currentLevel);
        String speedCode = getSpeedCode(learningSpeed);
        
        boolean hasMedium = currentLevel == 3 || learningSpeed == 3 || selfDiscipline == 3;
        String preferenceCode = hasMedium ? "M" : getPreferenceCode(preference);
        
        String disciplineCode = getDisciplineCode(selfDiscipline);

        return levelCode + speedCode + preferenceCode + disciplineCode;
    }

    private String getLevelCode(Integer level) {
        if (level >= 4 && level <= 5) {
            return "H";
        } else if (level >= 1 && level <= 2) {
            return "L";
        } else if (level == 3) {
            return "M";
        }
        return "M";
    }

    private String getSpeedCode(Integer speed) {
        if (speed >= 4 && speed <= 5) {
            return "F";
        } else if (speed >= 1 && speed <= 2) {
            return "S";
        } else if (speed == 3) {
            return "M";
        }
        return "M";
    }

    private String getPreferenceCode(String preference) {
        if (preference == null) {
            return "V";
        }
        String pref = preference.toLowerCase().trim();
        boolean hasVideoAudio = pref.contains("视频") || pref.contains("音频");
        boolean hasTextPractice = pref.contains("文本") || pref.contains("实践");
        
        if (hasVideoAudio && !hasTextPractice) {
            return "V";
        } else if (!hasVideoAudio && hasTextPractice) {
            return "T";
        } else if (hasVideoAudio && hasTextPractice) {
            return "M";
        }
        return "V";
    }

    private String getDisciplineCode(Integer discipline) {
        if (discipline >= 4 && discipline <= 5) {
            return "D";
        } else if (discipline >= 1 && discipline <= 2) {
            return "N";
        } else if (discipline == 3) {
            return "M";
        }
        return "M";
    }

    private String getLQAIName(String code) {
        return switch (code) {
            case "HFVD" -> "满级六边形战士";
            case "HSVD" -> "沉浸式鉴赏家";
            case "HFVN" -> "倍速通关狂魔";
            case "HSVN" -> "佛系观影学霸";
            case "LFVD" -> "热血逆袭主角";
            case "LSVD" -> "乖巧跟读生";
            case "LFVN" -> "考前极限赌徒";
            case "LSVN" -> "弹幕云学者";
            case "HFTD" -> "冷酷解题机器";
            case "HSTD" -> "深潜理论家";
            case "HFTN" -> "技巧流猎手";
            case "HSTN" -> "随性漫游者";
            case "LFTD" -> "苦行僧式信徒";
            case "LSTD" -> "蜗牛筑梦师";
            case "LFTN" -> "焦虑搜寻者";
            case "LSTN" -> "气氛组收藏家";
            case "MMMM" -> "绝对中庸大师";
            case "MFMD" -> "极速稳健派";
            case "MFMN" -> "聪明小滑头";
            case "MSMD" -> "笨鸟慢飞者";
            case "MSMN" -> "随缘佛系生";
            case "LMMD" -> "勤奋小白";
            case "LMMN" -> "迷茫跟风者";
            case "HMMD" -> "资深老油条";
            case "HMMN" -> "恃才傲物者";
            case "LFMD" -> "饥渴速成党";
            case "HFMD" -> "全能天花板";
            case "LSMD" -> "固执龟兔赛跑者";
            case "HSMD" -> "懒散大神";
            default -> "未知学格";
        };
    }

    private String getLQAIProfile(String code) {
        return switch (code) {
            case "HFVD" -> "他们不仅天赋异禀，而且极其勤奋。喜欢看视频来辅助理解复杂的知识点，追求极致的效率，是平台上的传说级用户。";
            case "HSVD" -> "真正的学习艺术家。他们不急于求成，喜欢通过高质量的视频课程细细品味定理的推导过程，享受知识带来的美感。";
            case "HFVN" -> "聪明但坐不住。仗着底子好，习惯开着2.0倍速刷课，只求快速搞定考点，多一秒都不愿意浪费在屏幕前。";
            case "HSVN" -> "把学习当成看剧。虽然基础很好，但全凭心情学习，喜欢躺着看教学视频，主打一个“随缘顿悟”。";
            case "LFVD" -> "典型的励志剧本持有者。虽然基础薄弱，但有着惊人的执行力，迫切希望通过高密度的视频轰炸来快速弥补差距。";
            case "LSVD" -> "老师最省心的学生。一步一个脚印，老老实实跟着视频做笔记，虽然反应不快，但胜在坚持，绝不掉队。";
            case "LFVN" -> "\"三天过四门\"的信仰者。平时完全不学，考前疯狂搜索“速成视频”，试图用最短的时间创造奇迹。";
            case "LSVN" -> "收藏夹里全是教程，播放量却是个位数。喜欢收藏各种学习视频，偶尔点开看看弹幕找乐子，实际学习效果随缘。";
            case "HFTD" -> "莫得感情的刷题机器。不喜欢听废话，直接看公式总结和题库，以极高的效率和准确率横扫试卷。";
            case "HSTD" -> "学院派代表。喜欢啃大部头的教材和论文，对概念的证明有执念，追求逻辑的绝对严谨。";
            case "HFTN" -> "擅长寻找捷径。喜欢在网上搜集“秒杀技巧”、“解题模板”等短文干货，用最少的力气拿最高的分数。";
            case "HSTN" -> "知识储备丰富但缺乏规划。书架上有很多书，想起来就翻两页，想不起来就落灰，纯粹凭兴趣阅读。";
            case "LFTD" -> "令人敬佩的笨鸟先飞者。知道自己不懂，所以强迫自己高强度背诵公式技巧、死磕题解，试图用汗水淹没困难。";
            case "LSTD" -> "缓慢但坚定的攀登者。每天只读懂一个定义，做对一道题，虽然进度条拉得很慢，但每一步都走得无比扎实。";
            case "LFTN" -> "间歇性踌躇满志。经常搜索“临时怎么救”、“零基础复习计划”，下载了一堆PDF资料，然后继续焦虑地玩手机。";
            case "LSTN" -> "差生文具多。买了精美的笔记本和全套教材，书皮都没拆，主要作用是摆在桌子上营造“我要学习了”的氛围。";
            case "MMMM" -> "\"隐形大多数\"。他们不偏科也不极端，主打一个稳字当头。视频能看进去就看，发文档也能凑合读，绝不内耗。";
            case "MFMD" -> "隐藏的效率专家。虽然目前成绩只是中等，但有着极强的执行力和求胜欲。给他们什么资料都能迅速消化。";
            case "MFMN" -> "典型的\"考前突击手\"。平时懒得要命，但脑瓜子转得快。临近考试，不管扔给他什么资料，都能在极短时间内抓住重点。";
            case "MSMD" -> "踏实的耕耘者。反应可能不够快，但胜在有耐心、守纪律，愿意花双倍的时间去磨。";
            case "MSMN" -> "真正的\"躺平\"一族。学不快也不想学快，没人盯着就不动，主打一个陪伴式学习。";
            case "LMMD" -> "令人动容的努力家。态度极其端正，不挑食不抱怨，靠着死记硬背和题海战术填补基础的深坑。";
            case "LMMN" -> "随波逐流的无头苍蝇。别人刷题他也刷，别人看课他也看，但\"一看就会，一做就废\"。";
            case "HMMD" -> "平台上的中流砥柱。底子好且自觉，按部就班就能保持高水平，是靠谱学霸。";
            case "HMMN" -> "让人又爱又恨的天才。平时不怎么学，但只要稍微发力，成绩依然吊打一片。";
            case "LFMD" -> "急于求成的焦虑者。基础差却总想走捷径，有一定执行力，但步子迈太大容易消化不良。";
            case "HFMD" -> "接近完美的六边形战士。天赋高、跑得快，偶尔偷懒，但依然是令人仰望的存在。";
            case "LSMD" -> "执着但方法不对的苦行者。基础差却喜欢死磕难题，进步非常缓慢。";
            case "HSMD" -> "大隐隐于市的扫地僧。喜欢慢悠悠地学甚至故意拖延，享受掌控知识的过程。";
            default -> "未知学格画像";
        };
    }

    /**
     * 获取用户在指定科目的学习画像
     */
    public LearningProfileDTO getProfile(Long userId, Long subjectId) {
        Optional<LearningProfile> profileOpt = learningProfileRepository.findByUserIdAndSubjectId(userId, subjectId);
        if (profileOpt.isEmpty()) {
            return null;
        }
        LearningProfile profile = profileOpt.get();
        LearningProfileDTO dto = new LearningProfileDTO();
        dto.setUserId(profile.getUserId());
        dto.setSubjectId(profile.getSubjectId());
        dto.setCurrentLevel(profile.getCurrentLevel());
        dto.setLearningSpeed(profile.getLearningSpeed());
        dto.setPreference(profile.getPreference());
        dto.setSelfDiscipline(profile.getSelfDiscipline());
        dto.setLqai(profile.getLQAI());
        dto.setLqaiCode(profile.getLQAI_code());
        dto.setProfile(profile.getProfile());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}