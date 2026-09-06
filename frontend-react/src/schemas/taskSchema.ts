import { z } from "zod";

export const taskSchema = z.object({
    title: z.string().trim().min(1, { message: "Title cannot be empty" }),
    description: z.string().trim().optional(),
    status: z.enum(["TODO", "IN_PROGRESS", "DONE"]),
    type: z.enum(["NOTE", "TREE"])
});

export type TaskFormData = z.infer<typeof taskSchema>;