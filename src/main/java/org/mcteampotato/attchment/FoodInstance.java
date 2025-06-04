package org.mcteampotato.attchment;

public class FoodInstance {
    private final FoodData.FoodInfo info;
    private int remainingTicks;

    /**
     * @param info          与 JSON 中定义相对应的 FoodInfo 实例
     * @param remainingTicks 初始的持续时长（以游戏刻 tick 为单位）
     */
    public FoodInstance(FoodData.FoodInfo info, int remainingTicks) {
        this.info = info;
        this.remainingTicks = remainingTicks;
    }

    /** 获取对应的 FoodInfo 元数据 */
    public FoodData.FoodInfo getInfo() {
        return info;
    }

    /** 获取当前剩余持续时长（tick） */
    public int getRemainingTicks() {
        return remainingTicks;
    }

    /** 
     * 获取当前剩余持续时间（秒），方便在 UI 上显示 
     * @return 向下取整的秒数 
     */
    public int getRemainingSeconds() {
        return remainingTicks / 20;
    }

    /** 游戏每刻调用一次，减少剩余持续刻数 */
    public void decrement() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    /** 是否已经过期（持续时间耗尽） */
    public boolean isExpired() {
        return remainingTicks <= 0;
    }
}