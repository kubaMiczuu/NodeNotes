export interface SubItemData {
    id: number,
    text: string,
    isDone: boolean,
    parentId?: number,
    taskId: number,
    children?: SubItemData[]
}

export interface TaskData {
    id?: number;
    status: "TODO" | "IN_PROGRESS" | "DONE";
    type: "NOTE" | "TREE";
    title: string;
    description?: string;
    createdAt: string;
    updatedAt: string;
    children?: SubItemData[];
}