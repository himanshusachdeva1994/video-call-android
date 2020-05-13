package com.himanshu.videocallsdkvendors.annotations.twilio

import androidx.annotation.IntDef

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 * This class has been picked from Twilio SDK sample app
 */

const val VIDEO = 0
const val NO_VIDEO = 1
const val SELECTED = 2

@IntDef(VIDEO, NO_VIDEO, SELECTED)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class State