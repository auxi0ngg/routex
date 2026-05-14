import React from 'react'
import { useAppSelector } from '../../store'
import clsx from 'clsx'
export default function DriversPage() {
  const dark = useAppSelector(s => s.ui.darkMode)
  return (
    <div className="animate-fade-in">
      <h1 className={clsx('text-2xl font-bold', dark ? 'text-white' : 'text-slate-900')}>Drivers</h1>
      <p className={clsx('mt-2 text-sm', dark ? 'text-slate-400' : 'text-slate-500')}>
        Full Drivers management interface.
      </p>
    </div>
  )
}
