interface ProfileInformationProps {
    username: string;
}

const ProfileInformation = ({username}: ProfileInformationProps) => {
    return (
        <div className="flex w-full gap-4 py-4 items-center border-b border-slate-200">

            <div className={`flex w-24 h-24 rounded-full border border-sky-200 bg-sky-100 justify-center items-center`}>
                <h1 className={`text-slate-800 text-3xl tracking-widest text-center font-bold`}>{username.substring(0, 3).toUpperCase()}</h1>
            </div>

            <div>
                <h2 className={`text-slate-800 text-xl tracking-wider font-bold`}>{username}</h2>
                <p className="text-slate-500 text-sm mt-1">Manage your account and preferences.</p>
            </div>

        </div>
    )
}

export default ProfileInformation;