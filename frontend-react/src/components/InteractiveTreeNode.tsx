import type {SubItemData} from "../types/task.ts";
import {axiosClient} from "../api/axiosClient.ts";

interface TreeNodeProps {
    item: SubItemData
    onAddChild: (item: SubItemData, text:string) => void
    onRemoveChild: (item: SubItemData) => void
    activeInputId: number | null,
    setActiveInputId: (id:number) => void,
    fetchTreeData: () => void,
}

const InteractiveTreeNode = ({item, onAddChild, onRemoveChild, activeInputId, setActiveInputId, fetchTreeData}: TreeNodeProps) => {

    const isAddingChild = activeInputId === item.id;

    const updateSubTaskStatus = async (itemToUpdate:SubItemData) => {
        await axiosClient.patch("/subitems/"+itemToUpdate?.id+"/status?isDone="+itemToUpdate.isDone);
        fetchTreeData();
    }

    return (
        <>
            <div className={`flex flex-row gap-1 items-center ${item?.parentId == null ? "mt-4" : ""}`}>

                {item?.parentId && (
                    <div className="min-w-4 border-b-2 border-slate-300"></div>
                )}

                <input checked={item?.isDone || false} type={"checkbox"} onChange={() => updateSubTaskStatus(item)} />

                <p className={`pl-2 gap-1.5 truncate ${item?.isDone ? "line-through text-slate-400" : ""} overflow-x-hidden`}>
                    {item?.text}
                </p>

                <button type={'button'} onClick={() => setActiveInputId(item.id)} className={`text-xs hover:scale-105 cursor-pointer hover:"`}>➕</button>

                <button type={'button'} onClick={() => onRemoveChild(item)} className={`text-xs hover:scale-105 cursor-pointer`}>🗑️</button>

            </div>

            {item?.children && item.children.length > 0 && (
                <div className="ml-6 flex flex-col">

                    {item.children.map((subChild: SubItemData, index: number, arr: SubItemData[]) => {
                        const isLast = index === arr.length - 1;

                        return (
                            <div key={subChild.id} className="relative">

                                <div className={`absolute w-0.5 bg-slate-300 ${isLast ? 'h-4' : 'h-full'}`}></div>

                                <InteractiveTreeNode item={subChild} onAddChild={onAddChild} onRemoveChild={onRemoveChild} fetchTreeData={fetchTreeData} activeInputId={activeInputId} setActiveInputId={setActiveInputId} />
                            </div>
                        )
                    })}
                </div>
            )}

            {isAddingChild && (

                <input type={"text"}
                       autoFocus={true}
                       onKeyDown={(e) => {
                           if(e.key === 'Enter') {
                               e.preventDefault();
                               onAddChild(item, e.currentTarget.value);
                               e.currentTarget.value = "";
                           }
                       }}
                       placeholder={"➕ Add sub item"}
                       className={`ml-6 outline-none text-slate-800`}
                />

            )}
        </>
    )
}

export default InteractiveTreeNode;