package base

import java.lang.reflect.Method
import java.lang.reflect.Field

object ResetPgTypeInfoCache {
  // See why we need this: 
  // 1. https://github.com/pgjdbc/pgjdbc/issues/3049#issuecomment-1834407304
  // 2. https://github.com/pgvector/pgvector/issues/922#issuecomment-4145861854
  def reset(conn: java.sql.Connection): Unit = {
    val pgConnectionClass: Class[_] = Class.forName("org.postgresql.jdbc.PgConnection")
    
    val connection: AnyRef = conn.unwrap(pgConnectionClass)

    val getTypeInfoMethod: Method = pgConnectionClass.getMethod("getTypeInfo")
    val typeInfo: AnyRef = getTypeInfoMethod.invoke(connection)

    val typeInfoCacheClass: Class[_] = Class.forName("org.postgresql.jdbc.TypeInfoCache")

    val oidToPgNameField: Field = typeInfoCacheClass.getDeclaredField("oidToPgName")
    val pgNameToOidField: Field = typeInfoCacheClass.getDeclaredField("pgNameToOid")
    val pgNameToSQLTypeField: Field = typeInfoCacheClass.getDeclaredField("pgNameToSQLType")
    val oidToSQLTypeField: Field = typeInfoCacheClass.getDeclaredField("oidToSQLType")

    oidToPgNameField.setAccessible(true)
    pgNameToOidField.setAccessible(true)
    pgNameToSQLTypeField.setAccessible(true)
    oidToSQLTypeField.setAccessible(true)

    val oidToPgName: java.util.Map[Int, String] = oidToPgNameField.get(typeInfo).asInstanceOf[java.util.Map[Int, String]]
    val pgNameToOid: java.util.Map[String, Int] = pgNameToOidField.get(typeInfo).asInstanceOf[java.util.Map[String, Int]]
    val pgNameToSQLType: java.util.Map[String, Int] = pgNameToSQLTypeField.get(typeInfo).asInstanceOf[java.util.Map[String, Int]]
    val oidToSQLType: java.util.Map[Int, Int] = oidToSQLTypeField.get(typeInfo).asInstanceOf[java.util.Map[Int, Int]]

    val iter: java.util.Iterator[java.util.Map.Entry[String, Int]] = pgNameToOid.entrySet.iterator()

    while (iter.hasNext) {
    val entry = iter.next()
    val typeName = entry.getKey
        val oid = entry.getValue
        oidToPgName.remove(oid)
        oidToSQLType.remove(oid)
        pgNameToSQLType.remove(typeName)
        iter.remove()
    }
  }
}