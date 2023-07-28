import java.util.regex.Pattern


fun getSecond(time:String) : Int{
    val units: List<String> = time.split(":")
    val hour = units[0].toInt()
    val minutes = units[1].toInt() //first element
    val seconds = units[2].toInt() //second element
    return 3600*hour+60 * minutes + seconds
}

fun isTimeFormatValid( time :String) : Boolean{
    if(Pattern.matches("^(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)\$", time)){
        return true
    }
    return false
}