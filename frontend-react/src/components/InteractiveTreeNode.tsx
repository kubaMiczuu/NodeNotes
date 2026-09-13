import type {SubItemData} from "../types/task.ts";
import {axiosClient} from "../api/axiosClient.ts";
import {useState} from "react";

interface TreeNodeProps {
    item: SubItemData
    onAddChild: (item: SubItemData, text:string) => void
    activeInputId: number | null,
    setActiveInputId: (id:number | null) => void,
    fetchTreeData: () => void,
}

const InteractiveTreeNode = ({item, onAddChild, activeInputId, setActiveInputId, fetchTreeData}: TreeNodeProps) => {

    const isAddingChild = activeInputId === item.id;
    const [isEditing, setIsEditing] = useState<boolean>(false);

    const updateSubTaskStatus = async (itemToUpdate:SubItemData) => {
        await axiosClient.patch(`/subitems/${itemToUpdate?.id}/status?isDone=${itemToUpdate.isDone}`);
        fetchTreeData();
    }

    const updateSubTaskText = async (itemToUpdate:SubItemData, text:string) => {
        await axiosClient.patch(`/subitems/${itemToUpdate?.id}/text`, null, {
            params: { text: text }
        });
        fetchTreeData();
    }

    const deleteSubItem = async (itemToDelete:SubItemData) => {
        await axiosClient.delete(`/subitems/${itemToDelete?.id}`);
        fetchTreeData();
    }

    return (
        <>
            <div className={`flex flex-row gap-1 items-center ${item?.parentId == null ? "mt-4" : ""}`}>

                {item?.parentId && (
                    <div className="min-w-4 border-b-2 border-slate-300"></div>
                )}

                <input checked={item?.isDone || false} type={"checkbox"} onChange={() => updateSubTaskStatus(item)} />

                {isEditing ? (
                    <input type={"text"}
                           defaultValue={item?.text}
                           autoFocus={true}
                           onBlur={() => setIsEditing(false)}
                           onKeyDown={(e) => {
                               if(e.key === 'Enter') {
                                   e.preventDefault();
                                   updateSubTaskText(item, e.currentTarget.value)
                                   setIsEditing(false);
                               }
                           }}
                           className={`pl-2 w-full outline-none text-slate-800 bg-slate-50 rounded-sm focus:ring-1 focus:ring-sky-400`}
                    />
                ) : (
                    <p className={` pl-2 gap-1.5 truncate ${item?.isDone ? "line-through text-slate-400" : "hover:text-slate-800"} overflow-x-hidden hover:scale-105 hover:cursor-pointer`}
                        onClick={() => setIsEditing(true)}>
                        {item?.text}
                    </p>
                )}


                <button type={'button'} onClick={() => setActiveInputId(item.id)} className={`text-xs hover:scale-105 cursor-pointer"`}>➕</button>

                <button type={'button'} onClick={() => deleteSubItem(item)} className={`text-xs hover:scale-105 cursor-pointer`}>🗑️</button>

            </div>

            {item?.children && item.children.length > 0 && (
                <div className="ml-6 flex flex-col">

                    {item.children.map((subChild: SubItemData, index: number, arr: SubItemData[]) => {
                        const isLast = index === arr.length - 1;

                        return (
                            <div key={subChild.id} className="relative">

                                <div className={`absolute w-0.5 bg-slate-300 ${isLast ? 'h-4' : 'h-full'}`}></div>

                                <InteractiveTreeNode item={subChild} onAddChild={onAddChild} fetchTreeData={fetchTreeData} activeInputId={activeInputId} setActiveInputId={setActiveInputId} />
                            </div>
                        )
                    })}
                </div>
            )}

            {isAddingChild && (

                <input type={"text"}
                       autoFocus={true}
                       onBlur={() => setActiveInputId(null)}
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