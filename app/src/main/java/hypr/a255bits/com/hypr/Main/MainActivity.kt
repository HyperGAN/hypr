package hypr.a255bits.com.hypr.Main

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.SubMenu
import android.widget.Spinner
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.pawegio.kandroid.start
import hypr.a255bits.com.hypr.BuyGenerator
import hypr.a255bits.com.hypr.CameraFragment.CameraActivity
import hypr.a255bits.com.hypr.Generator.Generator
import hypr.a255bits.com.hypr.MultiModels.MultiModels
import hypr.a255bits.com.hypr.R
import hypr.a255bits.com.hypr.Util.InAppBilling.IabHelper
import hypr.a255bits.com.hypr.WelcomeScreen.WelcomeScreen
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainMvp.view {

    val interactor by lazy { MainInteractor(applicationContext) }
    val presenter by lazy { MainPresenter(this, interactor, applicationContext) }
    private var modelSubMenu: SubMenu? = null
    var progressDownloadingModel: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)
        presenter.addModelsToNavBar(applicationContext)
        getInfoFromCameraActivity()
    }

    private fun getInfoFromCameraActivity() {
        presenter.isModelFragmentDisplayed = intent.hasExtra("indexInJson")
        if (presenter.isModelFragmentDisplayed) {
            presenter.indexInJson = intent.extras.getInt("indexInJson")
            presenter.image = intent.extras.getString("image")
            presenter.settingsHelper.setModelImagePath(presenter.image!!)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        presenter.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun buyModelPopup(skus: String, billingHelper: IabHelper?, generatorIndex: Int) {
        billingHelper?.launchPurchaseFlow(this, skus, 1001, { result, info ->
            presenter.handlePurchase(result, generatorIndex)
        }, "")
    }

    override fun popupSigninGoogle(googleSignInClient: GoogleApiClient) {
        alert("Would you like to sign into Google?", "sign into Google") {
            okButton { signinToGoogle(googleSignInClient) }
            cancelButton { dialog ->
                dialog.dismiss()
            }
        }.show()
    }

    override fun goBackToMainActivity() {
        intentFor<MainActivity>().start(applicationContext)
    }

    fun signinToGoogle(googleSignInClient: GoogleApiClient) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleSignInClient)
        startActivityForResult(signInIntent, presenter.SIGN_INTO_GOOGLE_RESULT)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == presenter.SIGN_INTO_GOOGLE_RESULT && resultCode == Activity.RESULT_OK) {
        }
    }

    override fun displayGeneratorsOnHomePage(generators: MutableList<BuyGenerator>) {
        val fragment: Fragment = WelcomeScreen.newInstance(generators, "")
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        presenter.startModel(0)
    }

    override fun startCameraActivity(indexInJson: Int) {
        startActivity(intentFor<CameraActivity>("indexInJson" to indexInJson))
    }

    override fun startMultipleModels(multiModels: MultiModels) {
        supportFragmentManager.beginTransaction().replace(R.id.container, multiModels).commit()
    }

    override fun displayBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun addModelsToNavBar(generator: Generator, index: Int) {
        modelSubMenu?.add(R.id.group1, index, index, generator.name)
        modelSubMenu?.getItem(index)?.setIcon(R.drawable.ic_lock)

    }

    override fun displayModelDownloadProgress() {
        progressDownloadingModel = progressDialog("Downloading Model") {
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
        }
        progressDownloadingModel?.show()

    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        presenter.onNavigationItemSelected(item)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun lockModelIfNotBought(googlePlayId: HashMap<String, String>) {
        val isModelBought = interactor.hasBoughtItem(googlePlayId["Generator"]!!)
        if (!isModelBought) {
            presenter.multiModel?.presenter?.lockModel(googlePlayId["Index"]?.toInt()!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun addControlNamesToToolbar(controlNames: List<String?>?) {
//        toolbar.title = ""
//        val adapter: SpinnerAdapter = ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, controlNames)
//        spinner.let { toolbar.removeView(it) }
//        if (controlNames!!.isNotEmpty()) {
//            this.spinner = Spinner(this)
//            spinner?.background?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
//            spinner?.adapter = adapter
//            toolbar.addView(spinner)
//        }
    }

    override fun closeDownloadingModelDialog() {
        progressDownloadingModel?.dismiss()
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopInAppBilling()
    }
}
