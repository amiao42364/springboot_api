package cn.miaomiao.api.service.impl;

import cn.miaomiao.api.constant.StringConstant;
import cn.miaomiao.api.dao.MajsoulCardAnswerDao;
import cn.miaomiao.api.dao.MajsoulCardDao;
import cn.miaomiao.api.entity.MajsoulCard;
import cn.miaomiao.api.entity.MajsoulCardAnswer;
import cn.miaomiao.api.service.MajsoulService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author miaomiao
 * @date 2019/6/5 10:45
 */
@Service
@Slf4j
public class MajsoulServiceImpl implements MajsoulService {

    @Resource
    private MajsoulCardDao cardDao;

    @Resource
    private MajsoulCardAnswerDao answerDao;

    /**
     * 获取模切牌谱
     *
     * @param type  1：随机获取，2：顺序获取
     * @param curId 当前id
     * @param limit 获取题目的数量
     * @return MajsoulCard
     */
    @Override
    public List<MajsoulCard> get(Integer type, Integer curId, Integer limit) {
        List<MajsoulCard> cards;
        if (1 == type) {
            cards = cardDao.getNext(curId, limit);
        } else {
            cards = cardDao.getRandom(limit);
        }

        if (cards == null) {
            return null;
        }
        // 获取所有题目的答案
        List<Integer> ids = cards.stream().map(MajsoulCard::getId).collect(Collectors.toList());
        QueryWrapper<MajsoulCardAnswer> qw = new QueryWrapper<>();
        qw.in("card_id", ids);
        List<MajsoulCardAnswer> allAnswers = answerDao.selectList(qw);
        if (allAnswers == null) {
            return cards;
        }
        // 组装答案
        for (MajsoulCard card : cards) {
            List<MajsoulCardAnswer> answers = allAnswers.stream()
                    .filter(answer -> answer.getCardId().equals(card.getId()))
                    .collect(Collectors.toList());
            card.setAnswers(answers);
        }
        return cards;
    }

    /**
     * 上传模切牌谱
     *
     * @param card MajsoulCard
     * @return success成功
     */
    @Override
    public String upload(MajsoulCard card) {
        // 验证数据格式
        String result = validCard(card);
        if (!StringConstant.SUCCESS_STR.equals(result)) {
            return result;
        }
        // id不存在则新增牌谱，否则只更新答案
        if (null == card.getId()) {
            // 新增前判断是否存在该牌谱
            QueryWrapper<MajsoulCard> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("card", card.getCard());
            queryWrapper.eq("score", card.getScore());
            queryWrapper.eq("session", card.getSession());
            queryWrapper.last("limit 1");
            MajsoulCard majsoulCard = cardDao.selectOne(queryWrapper);
            if (majsoulCard != null) {
                return "已存在该牌谱，序号为" + majsoulCard.getId();
            }
            cardDao.insert(card);
        }
        List<MajsoulCardAnswer> answers = card.getAnswers();
        if (answers == null) {
            return "请提交答案";
        }
        // 暂只支持一次提交一个答案
        MajsoulCardAnswer answer = answers.get(0);
        answer.setCardId(card.getId());
        // 判断该答案是否存在
        QueryWrapper<MajsoulCardAnswer> qw = new QueryWrapper<>();
        qw.eq("card_id", card.getId());
        qw.eq("key", answer.getKey());
        qw.last("limit 1");
        MajsoulCardAnswer obj = answerDao.selectOne(qw);
        if (obj != null) {
            return "已存在该答案";
        }
        answerDao.insert(answer);
        return StringConstant.SUCCESS_STR;
    }

    /**
     * 校验牌谱
     * @param card card
     * @return result
     */
    private String validCard(MajsoulCard card) {


        return StringConstant.SUCCESS_STR;
    }
}