package foreverse.afmsynthesis.afm

/**
 * Created by gbecan on 3/26/15.
 */
class StringDomain(initValues : Set[String],
                   initNullValue : String,
                   initLessThan : (String, String) => Boolean)
  extends Domain(initValues, initNullValue, initLessThan) {

}