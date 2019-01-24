package com.example.demo.mycustom;

import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.dao.MycustomDao3;
import com.example.demo.entity.Mycustom;

@Component
public class MycustomService {
	@Autowired
	ManagementService managementService;
	
	public void insert0(Mycustom mycustom) {
		managementService.executeCommand(new Command<Void>() {
			@Override
			public Void execute(CommandContext commandContext) {
				commandContext.getDbSqlSession().update("insertMy_custom", mycustom);
				return null;
			}
		});
	}
	public void insert1(Mycustom mycustom){
         managementService.executeCustomSql(new AbstractCustomSqlExecution<MycustomDao3,Boolean>(MycustomDao3.class) {
            @Override
            public Boolean execute(MycustomDao3 mycustomDao3) {
            	mycustomDao3.insertMy_custom(mycustom);
                return true;
            }
        });
    }
	public Mycustom select0(String id) {
		return managementService.executeCommand(new Command<Mycustom>() {
			@Override
			public Mycustom execute(CommandContext commandContext) {
				return (Mycustom) commandContext.getDbSqlSession().selectOne("selectMy_custom", id);
			}
		});
	}
	public Mycustom select1(String id){
		return managementService.executeCustomSql(new AbstractCustomSqlExecution<MycustomDao3,Mycustom>(MycustomDao3.class) {
           @Override
           public Mycustom execute(MycustomDao3 mycustomDao3) {
        	   return mycustomDao3.selectMy_custom(id);
           }
       });
   }
}
