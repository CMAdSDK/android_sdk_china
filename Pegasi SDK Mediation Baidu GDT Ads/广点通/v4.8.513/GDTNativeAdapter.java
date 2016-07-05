package com.cmcm.adsdk.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.cmcm.adsdk.Const;
import com.cmcm.adsdk.base.CMBaseNativeAd;
import com.cmcm.baseapi.ads.INativeAd;
import com.cmcm.utils.Commons;
import com.qq.e.ads.cfg.DownAPPConfirmPolicy;
import com.qq.e.ads.nativ.NativeAD;
import com.qq.e.ads.nativ.NativeADDataRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenhao on 15/12/1.
 */
public class GDTNativeAdapter extends NativeloaderAdapter implements NativeAD.NativeAdListener  {
    private static final int GDT_MAX_LOAD_NUM = 10;

    private Context mContext;
    private Map<String, Object> mExtras;

    @Override
    public void loadNativeAd(@NonNull Context context,
                                @NonNull Map<String, Object> extras) {

        mContext = context;
        mExtras = extras;
        String placementId = (String)mExtras.get(CMBaseNativeAd.KEY_PLACEMENT_ID);

        String gdtAppId = null;
        String gdtPlaceId = null;
        if (!TextUtils.isEmpty(placementId) && placementId.contains("_")) {
            String[] ids = placementId.split("_");
            if (ids.length >= 2) {
                gdtAppId = ids[0];
                gdtPlaceId = ids[1];
            }
        }
        Object loadSizeObject = mExtras.get(CMBaseNativeAd.KEY_LOAD_SIZE);
        int mLoadSize;
        if(loadSizeObject == null){
            mLoadSize = GDT_MAX_LOAD_NUM;
        }else {
            mLoadSize = (Integer)loadSizeObject;
        }
        NativeAD nativeAD = new NativeAD(mContext, gdtAppId, gdtPlaceId, this);
        nativeAD.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.NOConfirm);
        nativeAD.loadAD(mLoadSize);
    }

    @Override
    public int getReportRes() {
        return Const.res.gdt;
    }

    @Override
    public String getReportPkgName(String adTypeName) {
        return Const.pkgName.gdt;
    }

    @Override
    public String getAdKeyType() {
        return Const.KEY_GDT;
    }

    @Override
    public long getDefaultCacheTime() {
        return Const.cacheTime.gdt;
    }

    @Override
    public void onADLoaded(List<NativeADDataRef> list) {
        List<INativeAd> tempList = new ArrayList<INativeAd>();

        if(list != null && !list.isEmpty()) {
            for(NativeADDataRef nativeADDataRef : list){
                if(nativeADDataRef != null){
                    tempList.add(new TencentNativeAd(nativeADDataRef));
                }
            }
        }

        if (tempList.isEmpty()) {
            notifyNativeAdFailed("gdt.fake-fill.invalidad");
        } else {
            notifyNativeAdLoaded(tempList);
        }
    }

    @Override
    public void onNoAD(int i) {
        notifyNativeAdFailed(String.valueOf(i));
    }

    @Override
    public void onADStatusChanged(NativeADDataRef nativeADDataRef) {

    }

    public class TencentNativeAd extends CMBaseNativeAd {
        private View mAdView;
        private NativeADDataRef mNativeADDataRef;
        public TencentNativeAd(NativeADDataRef nativeADDataRef){
            mNativeADDataRef = nativeADDataRef;
            setUpData();
        }

        private void setUpData() {
            setTitle(mNativeADDataRef.getTitle());
            setAdCoverImageUrl(mNativeADDataRef.getImgUrl());
            setAdIconUrl(mNativeADDataRef.getIconUrl());
            setAdSocialContext(String.valueOf(mNativeADDataRef.getDownloadCount()));
            setAdBody(mNativeADDataRef.getDesc());
            setIsDownloadApp(mNativeADDataRef.isAPP());
            setAdCallToAction(converCallToAction(mNativeADDataRef));
            setAdStarRate(mNativeADDataRef.getAPPScore());
        }

        @Override
        public String getAdTypeName() {
            return Const.KEY_GDT;
        }

        @Override
        public boolean registerViewForInteraction(View view) {
            mAdView = view;
            if (null != mNativeADDataRef) {
                mNativeADDataRef.onExposured(view);
            }
            if (mImpressionListener != null)
                mImpressionListener.onLoggingImpression();
            return false;
        }

        @Override
        public void unregisterView() {
            if(null != mAdView) {
                mAdView = null;
            }
        }

        @Override
        public Object getAdObject() {
            return mNativeADDataRef;
        }

        @Override
        public void handleClick(){
            mNativeADDataRef.onClicked(mAdView);
        }

        //getAPPStatus()	获取应用状态，0：未开始下载；1：已安装；2：需要更新;4:下载中;8:下载完成;16:下载失败
        public String converCallToAction(NativeADDataRef ref) {
            int status = ref.getAPPStatus();
            String btnText = "";
            switch (status) {
                case 1:
                    btnText = "打开";
                    break;
                case 2:
                    btnText = "更新";
                case 4:
                    int progress = ref.getProgress();
                    btnText = String.valueOf((progress) + "%");
                    break;
                case 8:
                    btnText = "安装";
                    break;
                case 16:
                    btnText = "下载失败";
                    break;
                default:
                    btnText = "下载";
                    break;
            }
            return btnText;
        }
    }


}
