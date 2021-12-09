package coinhelper.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({OrmConfigurationSelector.class})
public @interface EnableOrm
{
  OrmType ormType() default OrmType.Jpa;
  
  boolean trxManagerNaming() default true;
}
