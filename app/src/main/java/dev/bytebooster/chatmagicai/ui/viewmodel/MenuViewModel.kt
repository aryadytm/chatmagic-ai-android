package dev.bytebooster.chatmagicai.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bytebooster.chatmagicai.data.RemoteConfigDatasource
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val remoteConfigDatasource: RemoteConfigDatasource
) : ViewModel() {

    val contactUrl = remoteConfigDatasource.contactUrl
    val feedbackUrl = remoteConfigDatasource.feedbackUrl
    val privacyPolicyUrl = remoteConfigDatasource.privacyPolicyUrl
    val termsOfServiceUrl = remoteConfigDatasource.termsOfServiceUrl

}