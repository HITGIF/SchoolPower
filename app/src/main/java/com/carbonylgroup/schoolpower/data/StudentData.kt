package com.carbonylgroup.schoolpower.data

/**
 * Created by Null on 2017/11/5.
 */

class StudentData(val studentInfo: StudentInformation, val attendances: List<Attendance>, val subjects: List<Subject>,
                  val disabled: Boolean, val disabledTitle: String?, val disabledMessage: String?, val extraInfo: ExtraInfo)

class ExtraInfo(val avatar:String)
