package com.rutgers.neemi.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "Task")
public class Task implements Serializable{

	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField
	String name;
	@DatabaseField
	String oid;
	@DatabaseField(canBeNull = true)
	boolean isGateway;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES Script(_id) ON DELETE CASCADE")
	Script script;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnDefinition = "INTEGER CONSTRAINT FK_NAME REFERENCES TaskDefinition(_id) ON DELETE CASCADE")
	TaskDefinition taskDefinition;

	ArrayList<String> list_of_oids = new ArrayList<>();
	ArrayList<Object> list_of_pids = new ArrayList<>();

	Object pid;


	ArrayList<TaskLocalValues> localValues;


	public Task(){
		this.localValues=new ArrayList<TaskLocalValues>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getPid() {
		return pid;
	}

	public void setPid(Object pid) {
		this.pid = pid;
	}

	public ArrayList<TaskLocalValues> getLocalValues() {
		return localValues;
	}
	public void setLocalValues(ArrayList<TaskLocalValues> locals) {
		this.localValues = locals;
	}
	public void addLocalValue(TaskLocalValues sublocal) {
		this.localValues.add(sublocal);
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	public void setTaskDefinition(TaskDefinition taskDefinition) {
		this.taskDefinition = taskDefinition;
	}

	public void addPid(Object pid){
		this.list_of_pids.add(pid);
	}

	public void addOid(String oid){
		this.list_of_oids.add(oid);
	}

	public ArrayList<String> getList_of_oids() {
		return list_of_oids;
	}

	public void setList_of_oids(ArrayList<String> list_of_oids) {
		this.list_of_oids = list_of_oids;
	}

	public ArrayList<Object> getList_of_pids() {
		return list_of_pids;
	}

	public void setList_of_pids(ArrayList<Object> list_of_pids) {
		this.list_of_pids = list_of_pids;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof Task) {
			Task t2 = (Task) obj;
			if (this.getPid() != null && t2.getPid() != null) {
				if (!this.getPid().getClass().getCanonicalName().equals(t2.getPid().getClass().getCanonicalName())) {
					return false;
				} else {
					if (this.getPid() instanceof Email) {
						if (((Email) this.getPid()).getId().equals(((Email) t2.getPid()).getId())) {
							return true;
						}
					} else if (this.getPid() instanceof Message) {
						if (((Message) this.getPid()).get_id() == ((Message) t2.getPid()).get_id()) {
							return true;
						}
					} else if (this.getPid() instanceof Transaction) {
						if (((Transaction) this.getPid()).getId().equals(((Transaction) t2.getPid()).getId())) {
							return true;
						}
					} else if (this.getPid() instanceof Event) {
						if (((Event) this.getPid()).getId().equals(((Event) t2.getPid()).getId())) {
							return true;
						}
					} else if (this.getPid() instanceof Photo) {
						if (((Photo) this.getPid()).getId().equals(((Photo) t2.getPid()).getId())) {
							return true;
						}
					} else if (this.getPid() instanceof Feed) {
						if (((Feed) this.getPid()).getId().equals(((Feed) t2.getPid()).getId())) {
							return true;
						}
					}
				}
			}
		}

		return false;

	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		if (this.getPid() instanceof Email) {
			return ((Email) this.getPid()).getId().hashCode();
		} else if (this.getPid() instanceof Message) {
			return new Integer(((Message) this.getPid()).get_id()).hashCode();
		} else if (this.getPid() instanceof Transaction) {
			return ((Transaction) this.getPid()).getId().hashCode();
		} else if (this.getPid() instanceof Event) {
			return ((Event) this.getPid()).getId().hashCode();
		} else if (this.getPid() instanceof Photo) {
			return ((Photo) this.getPid()).getId().hashCode();
		} else if (this.getPid() instanceof Feed) {
			return ((Feed) this.getPid()).getId().hashCode();
		}

		return 0;
	}

	public boolean isSame(Task t2) {
		if (this.getPid() != null && t2.getPid() != null) {
			if (!this.getPid().getClass().getCanonicalName().equals(t2.getPid().getClass().getCanonicalName())) {
				return false;
			} else {
				if (this.getPid() instanceof Email) {
					if (((Email) this.getPid()).getId().equals(((Email) t2.getPid()).getId())) {
						return true;
					}
				} else if(this.getPid() instanceof Message) {
					if (((Message) this.getPid()).get_id()==((Message) t2.getPid()).get_id()) {
						return true;
					}
				}else if(this.getPid() instanceof Transaction) {
					if (((Transaction) this.getPid()).getId().equals(((Transaction) t2.getPid()).getId())) {
						return true;
					}
				}else if(this.getPid() instanceof Event) {
					if (((Event) this.getPid()).getId().equals(((Event) t2.getPid()).getId())) {
						return true;
					}
				}else if(this.getPid() instanceof Photo) {
					if (((Photo) this.getPid()).getId().equals(((Photo) t2.getPid()).getId())) {
						return true;
					}
				}else if(this.getPid() instanceof Feed) {
					if (((Feed) this.getPid()).getId().equals(((Feed) t2.getPid()).getId())) {
						return true;
					}
				}
			}
		}

		return false;


	}


}
