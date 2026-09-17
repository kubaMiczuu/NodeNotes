import {axiosClient} from "../api/axiosClient.ts";
import type {SubItemData} from "../types/task.ts";
import {useState} from "react";
import InteractiveTreeNode from "./InteractiveTreeNode.tsx";

interface TreeContainerProps {
    taskId?: number,
    initialItems?: SubItemData[],
}

const InteractiveTreeContainer = ({taskId, initialItems}:TreeContainerProps) => {

    const [items, setItems] = useState<SubItemData[] | undefined>(initialItems);
    const [expandSignal, setExpandSignal] = useState<number>(0);
    const [collapseSignal, setCollapseSignal] = useState<number>(0);
    const [activeInputId, setActiveInputId] = useState<number | null>(null);

    const fetchTreeData = async () => {
        try {
            const response = await axiosClient.get("/tasks/"+taskId);
            setItems(response.data.children);
        } catch (error) {
            console.error("Tree rendering error", error);
        }
    }

    const onAddChild = async (parentItem: SubItemData | null, text: string) => {
        if(parentItem !== null && parentItem !== undefined) {
            await axiosClient.post(`/subitems/${parentItem.id}/children`, {text: text})
        } else {
            await axiosClient.post(`/tasks/${taskId}/subitems`, {text: text})
        }

        await fetchTreeData();
        setActiveInputId(null);
    }

    return (
        <div className={`flex flex-col w-full min-h-[calc(50vh-128px)] max-h-[calc(64vh-128px)]`}>

            <div className={`flex gap-10 items-center mb-2`}>

                <label className={`text-md text-slate-700 font-bold`}>
                    Tree Architecture
                </label>

                <button type={'button'} onClick={() => setExpandSignal(Date.now())}
                        className={`p-2 rounded-lg hover:bg-slate-200 transition duration-500 cursor-pointer`}>Expand All</button>

                <button type={'button'} onClick={() => setCollapseSignal(Date.now())}
                        className={`p-2 rounded-lg hover:bg-slate-200 transition duration-500 cursor-pointer`}>Collapse All</button>

            </div>

            <div className={`overflow-y-scroll w-full text-lg md:text-xl font-semibold text-slate-800 px-4 py-3 md:px-5 md:py-4 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-sky-400/50 focus:border-sky-400 transition duration-200`}>

                {items?.map((item: SubItemData) => (
                    <InteractiveTreeNode key={item?.id} item={item} onAddChild={onAddChild}  activeInputId={activeInputId} setActiveInputId={setActiveInputId} fetchTreeData={fetchTreeData} expandSignal={expandSignal} collapseSignal={collapseSignal} />
                ))}

                <input type={"text"}
                       autoFocus={true}
                       onBlur={() => setActiveInputId(null)}
                       onKeyDown={(e) => {
                           if(e.key === 'Enter') {
                               e.preventDefault();
                               onAddChild(null, e.currentTarget.value);
                               e.currentTarget.value = "";
                           }
                       }}
                       placeholder={"➕ Add task node"}
                       className={`outline-none text-slate-800`}
                />

            </div>

        </div>
    )
}
export default InteractiveTreeContainer