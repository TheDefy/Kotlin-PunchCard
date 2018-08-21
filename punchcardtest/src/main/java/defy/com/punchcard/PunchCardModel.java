package defy.com.punchcard;

import com.bbtree.baselib.net.ResultObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import defy.com.punchcard.base.PunchCardRxUtils;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

public class PunchCardModel {

    private static final String URL = "http://v3.card.bbtree.com:80/api/punch";

    static PunchCardModel instance;

    public static PunchCardModel getInstance() {
        if (instance == null) {
            instance = new PunchCardModel();
        }
        return instance;
    }

    private PunchCardModel() {
    }

    public Observable<ResultObject> uploadCardRecords(List<CardPunchRecord> cardRecords) {
        Map map = new HashMap();
        map.put("cardsRecord", cardRecords);
        return PunchCardRxUtils.postMap(URL, map)
                .map(PunchCardRxUtils.getMap())
                .filter(new Predicate<ResultObject>() {
                    @Override
                    public boolean test(ResultObject resultObject) throws Exception {
                        return resultObject.getCode() == 200;
                    }
                });
    }
}
