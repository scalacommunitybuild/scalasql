package scalasql.query

import scalasql.{MappedType, Queryable}
import scalasql.renderer.{Context, SqlStr}
import scalasql.utils.OptionPickler

trait Expr[T] extends SqlStr.Renderable {
  def mappedType: MappedType[T]
  final def toSqlQuery(implicit ctx: Context): SqlStr = {
    ctx.exprNaming.get(this.exprIdentity).getOrElse(toSqlExpr0)
  }

  def toSqlExpr0(implicit ctx: Context): SqlStr

  override def toString: String = throw new Exception(
    "Expr#toString is not defined. Use Expr#exprToString"
  )

  override def equals(other: Any): Boolean = throw new Exception(
    "Expr#equals is not defined. Use Expr#exprIdentity for your equality checks"
  )
  lazy val exprIdentity: Expr.Identity = new Expr.Identity()
  def exprToString: String = super.toString
}

object Expr {
  class Identity()

  implicit def ExprQueryable[E[_] <: Expr[_], T](implicit
      valueReader0: OptionPickler.Reader[T]
  ): Queryable[E[T], T] =
    new ExprQueryable[E, T]()

  class ExprQueryable[E[_] <: Expr[_], T](implicit valueReader0: OptionPickler.Reader[T])
      extends Queryable[E[T], T] {
    def walk(q: E[T]) = Seq(Nil -> q)

    def valueReader = valueReader0

    def valueReader(q: E[T]): OptionPickler.Reader[T] = valueReader0
  }


  def apply[T](f: Context => SqlStr)(implicit mappedType: MappedType[T]): Expr[T] = new Simple[T](f)
  class Simple[T](f: Context => SqlStr)(implicit val mappedType: MappedType[T]) extends Expr[T] {
    def toSqlExpr0(implicit ctx: Context): SqlStr = f(ctx)
  }

  implicit def from(x: Int): Expr[Int] = apply(x)
  implicit def from(x: Long): Expr[Long] = apply(x)
  implicit def from(x: Boolean): Expr[Boolean] = apply(x)
  implicit def from(x: Double): Expr[Double] = apply(x)
  implicit def from(x: String): Expr[String] = apply(x)
  implicit def apply[T](x: T)
                       (implicit conv: T => SqlStr.Interp,
                        mappedType0: MappedType[T]): Expr[T] = new Expr[T] {
    def mappedType = mappedType0
    override def toSqlExpr0(implicit ctx: Context): SqlStr =
      new SqlStr(Seq("", ""), Seq(conv(x)), false, Seq(this))
  }
}
