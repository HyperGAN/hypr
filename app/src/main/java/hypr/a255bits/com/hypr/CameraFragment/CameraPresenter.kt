package hypr.a255bits.com.hypr.CameraFragment

import android.content.Context
import android.net.Uri
import hypr.a255bits.com.hypr.Util.Analytics
import hypr.a255bits.com.hypr.Util.AnalyticsEvent
import hypr.a255bits.com.hypr.Util.ImageSaver


class CameraPresenter(val view: CameraMVP.view, val context: Context) : CameraMVP.presenter {

    val interactor: CameraInteractor by lazy{CameraInteractor(context)}
    val analytics = Analytics(context)
    override fun sendPictureToModel(jpeg: ByteArray?) {
        view.sendImageToModel(jpeg)
    }

    override fun getImageFromImageFileLocation(imageLocation: Uri) {
        val imageFromGallery: ByteArray? = ImageSaver().uriToByteArray(imageLocation, context)
        view.sendImageToModel(imageFromGallery)
    }

    fun captureImage() {
        view.takePicture()
        analytics.logEvent(AnalyticsEvent.TAKE_PICTURE)
    }

    override fun displayGallery() {
        view.displayGallery()
        analytics.logEvent(AnalyticsEvent.EXISTING_PHOTO)
    }
}