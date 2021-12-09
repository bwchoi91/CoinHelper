package coinhelper.database;

import java.util.List;

public abstract interface DomainObjectPackageable
{
	public abstract List<String> ormPackges();
	public abstract List<Class<?>> oxmClasses();
}
