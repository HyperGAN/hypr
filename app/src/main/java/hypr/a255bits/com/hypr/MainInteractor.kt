package hypr.a255bits.com.hypr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import org.jetbrains.anko.toast

class MainInteractor(val context: Context) : MainMvp.interactor {
    val detector: FaceDetector by lazy {
        FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build()
    }

    override fun getFacesFromBitmap(imageWithFaces: Bitmap, width: Int, height: Int, context: Context): MutableList<Bitmap> {
        val faceLocations: SparseArray<Face>? = getFaceLocations(imageWithFaces, context)
        val croppedFaces = getListOfFaces(faceLocations, imageWithFaces)
        return croppedFaces
    }

    private fun getFaceLocations(imageWithFaces: Bitmap, context: Context): SparseArray<Face>? {
        var locationOfFaces = SparseArray<Face>()
        if (detector.isOperational) {
            val frame = Frame.Builder().setBitmap(imageWithFaces).build()
            locationOfFaces = detector.detect(frame)
        } else {
            context.toast(context.resources.getString(R.string.failed_face_detection))
        }
        return locationOfFaces
    }

    private fun getListOfFaces(faceLocations: SparseArray<Face>?, imageWithFaces: Bitmap): MutableList<Bitmap> {
        val croppedFaces = mutableListOf<Bitmap>()
        val numOfFaces: Int = faceLocations?.size()!!
        repeat(numOfFaces) { index ->
            val faceLocation = faceLocations[index]
            val face = cropFaceOutOfBitmap(faceLocation, imageWithFaces)
            croppedFaces.add(face)
        }
        return croppedFaces
    }

    private fun cropFaceOutOfBitmap(face: Face, imageWithFaces: Bitmap): Bitmap {
        val centerOfFace = face.position
        val x : Int = intArrayOf(centerOfFace.x.toInt(), 0).max()!!
        val y : Int = intArrayOf(centerOfFace.y.toInt(), 0).max()!!

        val croppedFace = Bitmap.createBitmap(imageWithFaces, x, y, face.width.toInt(), face.height.toInt())
        return croppedFace
    }

    override fun uriToBitmap(imageLocation: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(context.contentResolver, imageLocation);
    }
}