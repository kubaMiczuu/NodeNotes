import type {SubItemData} from "../types/task.ts";
import {axiosClient} from "../api/axiosClient.ts";
import {useEffect, useState} from "react";

interface TreeNodeProps {
    item: SubItemData
    onAddChild: (item: SubItemData, text:string) => void
    activeInputId: number | null,
    setActiveInputId: (id:number | null) => void,
    fetchTreeData: () => void,
    expandSignal: number,
    collapseSignal: number
}

const InteractiveTreeNode = ({item, onAddChild, activeInputId, setActiveInputId, fetchTreeData, expandSignal, collapseSignal}: TreeNodeProps) => {

    const isAddingChild = activeInputId === item.id;
    const [isExpanded, setExpanded] = useState<boolean>(true);
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

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        if(expandSignal > 0) setExpanded(true);
    }, [expandSignal])

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        if(collapseSignal > 0) setExpanded(false);
    }, [collapseSignal]);

    return (
        <>
            <div className={`flex flex-row gap-1 items-center ${item?.parentId == null ? "mt-4" : ""}`}>

                {item?.parentId && (
                    <div className="min-w-4 border-b-2 border-slate-300"></div>
                )}

                <input checked={item?.isDone || false} type={"checkbox"} onChange={() => updateSubTaskStatus(item)} />

                {item?.children && item.children.length > 0 && (
                    <button type="button" onClick={() => setExpanded(!isExpanded)}
                        className={`p-1 rounded-md hover:bg-slate-200 transition-colors cursor-pointer text-slate-400 hover:text-slate-600`}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor"
                            className={`w-4 h-4 transition-transform duration-200 ${isExpanded ? "rotate-90" : "rotate-0"}`}
                        >
                            <path strokeLinecap="round" strokeLinejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
                        </svg>
                    </button>
                )}


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

            {item?.children && item.children.length > 0 && isExpanded && (
                <div className="ml-6 flex flex-col">

                    {item.children.map((subChild: SubItemData, index: number, arr: SubItemData[]) => {
                        const isLast = index === arr.length - 1;

                        return (
                            <div key={subChild.id} className="relative">

                                <div className={`absolute w-0.5 bg-slate-300 ${isLast ? 'h-4' : 'h-full'}`}></div>

                                <InteractiveTreeNode item={subChild} onAddChild={onAddChild} fetchTreeData={fetchTreeData} activeInputId={activeInputId} setActiveInputId={setActiveInputId} expandSignal={expandSignal} collapseSignal={collapseSignal} />
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