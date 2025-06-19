package com.petify.pet.context;

public class UserContextHolder {
    
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    public static void setContext(UserContext userContext) {
        CONTEXT_HOLDER.set(userContext);
    }
    
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    public static Long getCurrentUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }
    
    public static String getCurrentUsername() {
        UserContext context = getContext();
        return context != null ? context.getUsername() : null;
    }
    
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}