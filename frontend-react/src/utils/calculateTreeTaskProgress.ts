import type {SubItemData} from "../types/task.ts";

export const calculateTreeTaskProgress = (items: SubItemData[] | undefined) => {
    if (!items || items.length === 0) return 0;

    const { total, doneItems } = count(items);

    if (total === 0) return 0;
    return (doneItems / total) * 100;
}

const count = (items: SubItemData[] | undefined) => {
    let total: number = 0;
    let doneItems: number = 0;

    if (items) {
        for (const item of items) {
            total += 1;
            if (item.isDone) doneItems += 1;

            if (item.children && item.children.length > 0) {
                const childCalculations = count(item.children);
                total += childCalculations.total;
                doneItems += childCalculations.doneItems;
            }
        }
    }

    return { total, doneItems };
}