package usql
import scala.language.experimental.macros
import upickle.default.{Reader, Writer, macroR, macroW}
trait Table0 {
  def tableName: String
}

abstract class Table[V[_[_]] <: Product]()(implicit name: sourcecode.Name) extends Table0 {
  val tableName = name.value.toLowerCase
  implicit def self: Table[V] = this

  def items[T[_]](t: V[T]): Seq[T[_]] = t.productIterator.map(_.asInstanceOf[T[_]]).toSeq

  implicit def queryable[X[_] <: Expr[_]]: Queryable[V[X]] = new Queryable[V[X]] {
    def toAtomics(t: V[X]): Seq[Atomic[_]] = items(t).flatMap(_.toAtomics)
    def toTables(t: V[X]): Set[Table0] = items(t).flatMap(_.toTables).toSet
  }

  def metadata: Metadata[V]

  def initMetadata[V[_[_]] <: Product](): Metadata[V] = macro Metadata.applyImpl[V]

  implicit def valueReader: Reader[V[Val]] = metadata.valueReader
  implicit def queryWriter: Writer[V[Atomic]] = metadata.queryWriter
  implicit def queryWriter2: Writer[V[Column]] = metadata.queryWriter2
  def query = metadata.query
}

class Metadata[V[_[_]]](val valueReader: Reader[V[Val]],
                        val queryWriter: Writer[V[Atomic]],
                        val queryWriter2: Writer[V[Column]],
                        val query: Query[V[Column]])


object Metadata{
  private trait Dummy[T[_]] extends Product
  def applyImpl[V[_[_]] <: Product](c: scala.reflect.macros.blackbox.Context)
                                   ()
                                   (implicit wtt: c.WeakTypeTag[V[Any]]): c.Expr[Metadata[V]] = {
    import c.universe._

    val applyParameters = c.prefix.actualType.member(TermName("apply")).info.paramLists.head

    val queryParams = for(applyParam <- applyParameters) yield {
      val name = applyParam.name
      if (c.prefix.actualType.member(name) != NoSymbol){
        q"${c.prefix}.${TermName(name.toString)}"
      }else{
        q"_root_.usql.Column[${applyParam.info.typeArgs.head}]()(${name.toString}, ${c.prefix})"
      }
    }


    c.Expr[Metadata[V]](
      q"""
        new _root_.usql.Metadata[$wtt](
          _root_.upickle.default.macroR,
          _root_.upickle.default.macroW,
          _root_.upickle.default.macroW,
          _root_.usql.Query(new $wtt(..$queryParams))
        )
       """
    )
  }
}
