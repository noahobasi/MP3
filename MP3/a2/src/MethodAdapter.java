import java.io.IOException;

import org.objectweb.asm.*;

public class MethodAdapter extends MethodVisitor implements Opcodes {
    public MethodAdapter(MethodVisitor mv) {
        super(ASM5,mv);
    }
    @Override
    public void visitInsn(int opcode) {
    	switch (opcode) {
    	case Opcodes.MONITORENTER:
    	mv.visitInsn(Opcodes.DUP);
    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log",
    	"logLock","(Ljava/lang/Object;)V",false);
    	break;
    	case Opcodes.MONITOREXIT:
    	mv.visitInsn(Opcodes.DUP);
    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log",
    	"logUnlock","(Ljava/lang/Object;)V",false);
    	break;
    	case Opcodes.IRETURN:
    	case Opcodes.LRETURN:
    	case Opcodes.FRETURN:
    	case Opcodes.DRETURN:
    	case Opcodes.ARETURN:
    	case Opcodes.RETURN:
    	case Opcodes.ATHROW:
    	{
    	if(isSynchronized){
    	if(isStatic){
    	mv.visitInsn(Opcodes.ACONST_NULL);
    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log",
    	"logUnlock","(Ljava/lang/Object;)V",false);
    	}
    	else{
    	mv.visitVarInsn(Opcodes.ALOAD, 0);
    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log",
    	"logUnlock","(Ljava/lang/Object;)V",false);
    	}
    	}
    	}
    	default:break;
    	}
    	mv.visitInsn(opcode);
    	}

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    	switch (opcode) {
        	case INVOKEVIRTUAL:
        		//check if it is "Thread.start()"
        		if(isThreadClass(owner)&&name.equals("start")&&desc.equals("()V")) {
	            	mv.visitInsn(DUP);
	        		mv.visitMethodInsn(INVOKESTATIC, "Log", "logStart",
	        				"(Ljava/lang/Thread;)V",false);
				}//check if it is "Thread.join()"
        		else if(isThreadClass(owner)&&name.equals("join")&&desc.equals("()V")) {

    			} //check if it is "Object.wait()"
            	else if(name.equals("wait")&&
                				(desc.equals("()V")||desc.equals("(J)V")||desc.equals("(JI)V"))) {

        		} //check if it is "Object.notify()"
                else if(name.equals("notify")&&desc.equals("()V")) {

            	}//check if it is "Object.notifyAll()"
                else if(name.equals("notifyAll")&&desc.equals("()V")) {

                				}
        	default: mv.visitMethodInsn(opcode, owner, name, desc,itf);
    	}

    }

    private boolean isThreadClass(String cname)
    {
    	while(!cname.equals("java/lang/Object"))
    	{
    		if(cname.equals("java/lang/Thread"))
    			return true;

    		try {
				ClassReader cr= new ClassReader(cname);
				cname = cr.getSuperName();
			} catch (IOException e) {
				return false;
			}
    	}
    	return false;
    }
}