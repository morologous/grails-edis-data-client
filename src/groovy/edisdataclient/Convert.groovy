package edisdataclient

// Convert.from( String ).to( Date ).using { }
class Convert {

    private from

    private to

    private Convert(clazz) { from = clazz }

    static def from(clazz) {
        new Convert(clazz)
    }

    def to(clazz) {
        to = clazz
        return this
    }

    def using(closure) {
        def originalAsType = from.metaClass.getMetaMethod('asType', [] as Class[])
        from.metaClass.asType = { Class clazz ->
            if( clazz == to ) {
                //closure.setProperty('value', delegate)
                closure(delegate)
            } else {
                originalAsType.doMethodInvoke(delegate, clazz)
            }
        }
    }
}
